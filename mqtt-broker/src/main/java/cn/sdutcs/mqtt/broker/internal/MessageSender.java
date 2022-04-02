package cn.sdutcs.mqtt.broker.internal;

import cn.sdutcs.mqtt.broker.domain.DeviceMessage;
import cn.sdutcs.mqtt.broker.domain.MqttMessageHelper;
import cn.sdutcs.mqtt.broker.domain.DeviceMessageBuilder;
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
    private InternalCommunication internalCommunication;
    @Autowired
    private IDupPublishMessageStoreService dupPublishMessageStoreService;
    @Autowired
    private ConcurrentHashMap<String, ChannelId> channelIdMap;
    @Autowired
    private ChannelGroup channelGroup;

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
                    DeviceMessage deviceMessage = DeviceMessageBuilder.buildDeviceMessage(retain, dup, respQoS.value(), topic, messageBytes);
                    MqttPublishMessage pubMessage = MqttMessageHelper.getPubMessage(deviceMessage, 0);

                    LOGGER.info("PUBLISH [C <- S] - from clientId: {} to clientId: {}, topic: {}, Qos: {}", fromClientId, clientId, topic, respQoS.value());
                    this.sendToChannel(clientId, pubMessage, messageBytes);
                }
                // Qos=1 2
                if (respQoS == MqttQoS.AT_LEAST_ONCE || respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    DeviceMessage deviceMessage = DeviceMessageBuilder.buildDeviceMessage(retain, dup, respQoS.value(), topic, messageBytes);
                    MqttPublishMessage pubMessage = MqttMessageHelper.getPubMessage(deviceMessage, messageId);

                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(clientId)
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
                    dupPublishMessageStoreService.put(clientId, dupPublishMessageStore);

                    LOGGER.info("PUBLISH [C <- S] - from clientId: {} to clientId: {}, topic: {}, Qos: {}, messageId: {}", fromClientId, clientId, topic, respQoS.value(), messageId);
                    this.sendToChannel(clientId, pubMessage, messageBytes);
                }
            }
        });
    }

    private void sendToChannel(String clientId, MqttPublishMessage publishMessage, byte[] messageBytes) {
        SessionStore sessionStore = sessionStoreService.get(clientId);
        ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
        if (channelId != null) {
            Channel channel = channelGroup.find(channelId);
            if (channel != null) {
                channel.writeAndFlush(publishMessage);
                // 消息发送完毕，session续期
                sessionStoreService.expire(clientId, sessionStore.getExpire());

                // kafka 总线引擎转发 2022.04.01
                InternalMessage internalMessage = new InternalMessage().setTopic(publishMessage.variableHeader().topicName())
                        .setMqttQoS(publishMessage.fixedHeader().qosLevel().value())
                        .setMessageBytes(messageBytes)
                        .setDup(false).setRetain(false).setClientId(clientId);
                internalCommunication.internalSend(internalMessage);
            } else {
                // 没有找到channel，说明会话已经结束
                this.sendWillMessage(sessionStore.getClientId(), sessionStore.getWillMessage());
                sessionStoreService.remove(clientId);
            }
        }
    }

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
