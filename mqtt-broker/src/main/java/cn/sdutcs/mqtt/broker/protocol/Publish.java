package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.internal.InternalCommunication;
import cn.sdutcs.mqtt.broker.internal.InternalMessage;
import cn.sdutcs.mqtt.broker.service.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PUBLISH连接处理
 */
public class Publish {

    private static final Logger LOGGER = LoggerFactory.getLogger(Publish.class);

    private final BrokerConfig brokerProperties;
    private final ConcurrentHashMap<String, ChannelId> channelIdMap;
    private final ChannelGroup channelGroup;
    private final InternalCommunication internalCommunication;

    private final ISessionStoreService sessionStoreService;
    private final ISubscribeStoreService subscribeStoreService;
    private final IMessageIdService messageIdService;
    private final IRetainMessageStoreService retainMessageStoreService;
    private final IDupPublishMessageStoreService dupPublishMessageStoreService;

    public Publish(BrokerConfig brokerProperties, ConcurrentHashMap<String, ChannelId> channelIdMap, ChannelGroup channelGroup,
                   InternalCommunication internalCommunication,
                   ISessionStoreService sessionStoreService,
                   ISubscribeStoreService subscribeStoreService,
                   IMessageIdService messageIdService,
                   IRetainMessageStoreService retainMessageStoreService,
                   IDupPublishMessageStoreService dupPublishMessageStoreService) {
        this.brokerProperties = brokerProperties;
        this.channelIdMap = channelIdMap;
        this.channelGroup = channelGroup;
        this.internalCommunication = internalCommunication;
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
        this.messageIdService = messageIdService;
        this.retainMessageStoreService = retainMessageStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
    }

    public void processPublish(Channel channel, MqttPublishMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        // publish 更新session失效时间
        if (sessionStoreService.containsKey(clientId)) {
            SessionStore sessionStore = sessionStoreService.get(clientId);
            ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
            if (brokerProperties.getId().equals(sessionStore.getBrokerId()) && channelId != null) {
                sessionStoreService.expire(clientId, sessionStore.getExpire());
            }
        }

        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName()).
                setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes).
                setDup(false).setRetain(false).setClientId(clientId);

        // Qos=0
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_MOST_ONCE) {
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);

        }
        // Qos=1
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        // Qos=2
        if (msg.fixedHeader().qosLevel() == MqttQoS.EXACTLY_ONCE) {
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubRecMessage(channel, msg.variableHeader().packetId());
        }
        // retain=1, 保留消息
        if (msg.fixedHeader().isRetain()) {
            if (messageBytes.length == 0) {
                retainMessageStoreService.remove(msg.variableHeader().topicName());
            } else {
                RetainMessageStore retainMessageStore = new RetainMessageStore().setTopic(msg.variableHeader().topicName())
                        .setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes);
                retainMessageStoreService.put(msg.variableHeader().topicName(), retainMessageStore);
            }
        }
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = subscribeStoreService.search(topic);
        subscribeStores.forEach(subscribeStore -> {
            String clientId = subscribeStore.getClientId();
            if (sessionStoreService.containsKey(clientId)) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                // Qos=0
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0),
                            Unpooled.buffer().writeBytes(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", clientId, topic, respQoS.value());
                    this.sendToChannel(clientId, publishMessage);
                }
                // Qos=1
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId),
                            Unpooled.buffer().writeBytes(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", clientId, topic, respQoS.value(), messageId);
                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(clientId)
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
                    dupPublishMessageStoreService.put(clientId, dupPublishMessageStore);
                    this.sendToChannel(clientId, publishMessage);
                }
                // Qos=2
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId),
                            Unpooled.buffer().writeBytes(messageBytes));
                    LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", clientId, topic, respQoS.value(), messageId);
                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(clientId)
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
                    dupPublishMessageStoreService.put(clientId, dupPublishMessageStore);
                    this.sendToChannel(clientId, publishMessage);
                }
            }
        });
    }

    private void sendToChannel(String clientId, MqttPublishMessage publishMessage) {
        SessionStore sessionStore = sessionStoreService.get(clientId);
        ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
        if (channelId != null) {
            Channel channel = channelGroup.find(channelId);
            if (channel != null) channel.writeAndFlush(publishMessage);
        }
    }

    private void sendPubAckMessage(Channel channel, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(Channel channel, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubRecMessage);
    }
}
