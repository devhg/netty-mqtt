package cn.sdutcs.mqtt.broker.internal;

import cn.sdutcs.mqtt.broker.domain.DeviceMessage;
import cn.sdutcs.mqtt.broker.domain.MqttMessageHelper;
import cn.sdutcs.mqtt.broker.domain.DeviceMessageBuilder;
import cn.sdutcs.mqtt.broker.service.PacketService;
import cn.sdutcs.mqtt.common.message.DupPublishMessageStore;
import cn.sdutcs.mqtt.common.message.IDupPublishMessageStoreService;
import cn.sdutcs.mqtt.common.message.IMessageIdService;
import cn.sdutcs.mqtt.common.session.ISessionStoreService;
import cn.sdutcs.mqtt.common.session.SessionStore;
import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    private ISubscribeStoreService subscribeStoreService;
    @Autowired
    private IMessageIdService messageIdService;
    @Autowired
    private ISessionStoreService sessionStoreService;
    @Autowired
    private IDupPublishMessageStoreService dupPublishMessageStoreService;
    @Autowired
    private ConcurrentHashMap<String, ChannelId> channelIdMap;
    @Autowired
    private ChannelGroup channelGroup;
    @Autowired
    private PacketService packetService;

    public void sendPublishMessage(String fromClientId, String topic, MqttQoS mqttQoS,
                                   byte[] messageBytes, boolean retain, boolean dup) {
        // 分发给订阅该topic的所有session
        List<SubscribeStore> subscribeStores = subscribeStoreService.search(topic);
        subscribeStores.forEach(subscribeStore -> {
            String clientId = subscribeStore.getClientId();
            if (sessionStoreService.containsKey(clientId)) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                // Qos=0
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    // MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    //         new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                    //         new MqttPublishVariableHeader(topic, 0),
                    //         Unpooled.buffer().writeBytes(messageBytes));

                    DeviceMessage deviceMessage = DeviceMessageBuilder.buildDeviceMessage(retain, dup, respQoS.value(), topic, messageBytes);
                    MqttPublishMessage pubMessage = MqttMessageHelper.getPubMessage(deviceMessage, 0);

                    LOGGER.info("PUBLISH [C <- S] - from clientId: {} to clientId: {}, topic: {}, Qos: {}", fromClientId, clientId, topic, respQoS.value());
                    this.sendToChannel(clientId, pubMessage);
                }
                // Qos=1
                if (respQoS == MqttQoS.AT_LEAST_ONCE || respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    // MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    //         new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                    //         new MqttPublishVariableHeader(topic, messageId),
                    //         Unpooled.buffer().writeBytes(messageBytes));

                    DeviceMessage deviceMessage = DeviceMessageBuilder.buildDeviceMessage(retain, dup, respQoS.value(), topic, messageBytes);
                    MqttPublishMessage pubMessage = MqttMessageHelper.getPubMessage(deviceMessage, messageId);

                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(clientId)
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
                    dupPublishMessageStoreService.put(clientId, dupPublishMessageStore);

                    LOGGER.info("PUBLISH [C <- S] - from clientId: {} to clientId: {}, topic: {}, Qos: {}, messageId: {}", fromClientId, clientId, topic, respQoS.value(), messageId);
                    this.sendToChannel(clientId, pubMessage);
                }
                // Qos=2
                // if (respQoS == MqttQoS.EXACTLY_ONCE) {
                //     int messageId = messageIdService.getNextMessageId();
                //     MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                //             new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                //             new MqttPublishVariableHeader(topic, messageId),
                //             Unpooled.buffer().writeBytes(messageBytes));
                //
                //
                //     DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(clientId)
                //             .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
                //     dupPublishMessageStoreService.put(clientId, dupPublishMessageStore);
                //     LOGGER.info("PUBLISH - from clientId: {} to clientId: {}, topic: {}, Qos: {}, messageId: {}", fromClientId, clientId, topic, respQoS.value(), messageId);
                //     this.sendToChannel(clientId, publishMessage);
                // }
            }
        });
    }

    private void sendToChannel(String clientId, MqttPublishMessage publishMessage) {
        SessionStore sessionStore = sessionStoreService.get(clientId);
        ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
        if (channelId != null) {
            Channel channel = channelGroup.find(channelId);
            if (channel != null) {
                channel.writeAndFlush(publishMessage);
                // 消息发送完毕，session续期
                sessionStoreService.expire(clientId, sessionStore.getExpire());
            } else {
                // 没有找到channel，说明会话已经结束
                this.sendWillMessage(sessionStore.getClientId(), sessionStore.getWillMessage());
                sessionStoreService.remove(clientId);
            }
        }
    }

    /**
     * 发送遗嘱消息
     */
    public void sendWillMessage(String fromClientId, MqttPublishMessage willMessage) {
        if (willMessage == null) {
            return;
        }
        this.sendPublishMessage(
                fromClientId,
                willMessage.variableHeader().topicName(),
                willMessage.fixedHeader().qosLevel(),
                willMessage.payload().array(),
                willMessage.fixedHeader().isRetain(),
                willMessage.fixedHeader().isDup()
        );
    }

    public void sendPubAckMessage(String clientId, Channel channel, int messageId) {
        MqttPubAckMessage pubAckMessage = MqttMessageHelper.getPubAckMessage(messageId);
        channel.writeAndFlush(pubAckMessage);
    }

    public void sendPubRecMessage(String clientId, Channel channel, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);

        LOGGER.info("PUBREC - to clientId: {}, messageId: {}", clientId, messageId);
        channel.writeAndFlush(pubRecMessage);
    }
}
