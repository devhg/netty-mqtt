package cn.sdutcs.mqtt.broker.internal;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息发送
 */
@Component
public class InternalSendServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalSendServer.class);
    @Autowired
    private BrokerConfig brokerProperties;
    @Autowired
    private ChannelGroup channelGroup;
    @Autowired
    private ConcurrentHashMap<String, ChannelId> channelIdMap;

    /**
     * 发送消息到Topic(排除自身clientId)
     */
    public void sendPublishMessage(String clientId, String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        // List<SubscribeStore> subscribeStores = subscribeStoreService.search(topic);
        // subscribeStores.forEach(subscribeStore -> {
        //     if (!clientId.equals(subscribeStore.getClientId()) && sessionStoreService.containsKey(subscribeStore.getClientId())) {
        //         // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
        //         MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
        //         if (respQoS == MqttQoS.AT_MOST_ONCE) {
        //             MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
        //                     new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
        //                     new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
        //             LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
        //             SessionStore sessionStore = sessionStoreService.get(subscribeStore.getClientId());
        //             ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
        //             if (channelId != null) {
        //                 Channel channel = channelGroup.find(channelId);
        //                 if (channel != null) channel.writeAndFlush(publishMessage);
        //             }
        //         }
        //         if (respQoS == MqttQoS.AT_LEAST_ONCE) {
        //             int messageId = messageIdService.getNextMessageId();
        //             MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
        //                     new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
        //                     new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
        //             LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
        //             SessionStore sessionStore = sessionStoreService.get(subscribeStore.getClientId());
        //             ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
        //             if (channelId != null) {
        //                 Channel channel = channelGroup.find(channelId);
        //                 if (channel != null) channel.writeAndFlush(publishMessage);
        //             }
        //         }
        //         if (respQoS == MqttQoS.EXACTLY_ONCE) {
        //             int messageId = messageIdService.getNextMessageId();
        //             MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
        //                     new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
        //                     new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
        //             LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
        //             SessionStore sessionStore = sessionStoreService.get(subscribeStore.getClientId());
        //             ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
        //             if (channelId != null) {
        //                 Channel channel = channelGroup.find(channelId);
        //                 if (channel != null) channel.writeAndFlush(publishMessage);
        //             }
        //         }
        //     }
        // });
    }
}
