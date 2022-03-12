package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.internal.InternalCommunication;
import cn.sdutcs.mqtt.broker.internal.InternalMessage;
import cn.sdutcs.mqtt.broker.internal.MessageSender;
import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.common.message.IRetainMessageStoreService;
import cn.sdutcs.mqtt.common.session.ISessionStoreService;
import cn.sdutcs.mqtt.common.session.SessionStore;
import cn.sdutcs.mqtt.common.message.RetainMessageStore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * PUBLISH连接处理
 */
public class Publish {

    private static final Logger LOGGER = LoggerFactory.getLogger(Publish.class);


    private final BrokerConfig brokerProperties;
    private final ConcurrentHashMap<String, ChannelId> channelIdMap;

    private final MessageSender messageSender;
    private final InternalCommunication internalCommunication;

    private final ISessionStoreService sessionStoreService;
    private final IRetainMessageStoreService retainMessageStoreService;

    public Publish(BrokerConfig brokerProperties, ConcurrentHashMap<String, ChannelId> channelIdMap,
                   InternalCommunication internalCommunication,
                   MessageSender messageSender,
                   ISessionStoreService sessionStoreService,
                   IRetainMessageStoreService retainMessageStoreService) {
        this.brokerProperties = brokerProperties;
        this.channelIdMap = channelIdMap;
        this.internalCommunication = internalCommunication;
        this.messageSender = messageSender;
        this.sessionStoreService = sessionStoreService;
        this.retainMessageStoreService = retainMessageStoreService;
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

        MqttQoS qosLevel = msg.fixedHeader().qosLevel();
        String topicName = msg.variableHeader().topicName();

        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);

        InternalMessage internalMessage = new InternalMessage().setTopic(topicName).setMqttQoS(qosLevel.value()).
                setMessageBytes(messageBytes).setDup(false).setRetain(false).setClientId(clientId);

        // Qos=0
        if (qosLevel == MqttQoS.AT_MOST_ONCE) {
            // internalCommunication.internalSend(internalMessage);
            messageSender.sendPublishMessage(clientId, topicName, qosLevel, messageBytes, false, false);

        }
        // Qos=1
        if (qosLevel == MqttQoS.AT_LEAST_ONCE) {
            // internalCommunication.internalSend(internalMessage);
            messageSender.sendPublishMessage(clientId, topicName, qosLevel, messageBytes, false, false);
            messageSender.sendPubAckMessage(clientId, channel, msg.variableHeader().packetId());
        }
        // Qos=2
        if (qosLevel == MqttQoS.EXACTLY_ONCE) {
            // internalCommunication.internalSend(internalMessage);
            messageSender.sendPublishMessage(clientId, topicName, qosLevel, messageBytes, false, false);
            messageSender.sendPubAckMessage(clientId, channel, msg.variableHeader().packetId());
            // this.sendPubRecMessage(channel, msg.variableHeader().packetId());
        }

        // retain=1, 保留消息
        if (msg.fixedHeader().isRetain()) {
            if (messageBytes.length == 0) {
                // isRetain == true && length = 0 说明删除上一个remain消息
                retainMessageStoreService.remove(msg.variableHeader().topicName());
            } else {
                RetainMessageStore retainMessageStore = new RetainMessageStore().setTopic(topicName)
                        .setMqttQoS(qosLevel.value()).setMessageBytes(messageBytes);
                retainMessageStoreService.put(topicName, retainMessageStore);
            }
        }
    }
}
