package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.service.PacketService;
import cn.sdutcs.mqtt.common.session.ISessionStoreService;
import cn.sdutcs.mqtt.common.session.SessionStore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * PINGREQ连接处理
 */
public class PingReq {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingReq.class);

    private final PacketService packetService;
    private final BrokerConfig brokerProperties;
    private final ConcurrentHashMap<String, ChannelId> channelIdMap;
    private final ISessionStoreService sessionStoreService;

    public PingReq(PacketService packetService,
                   BrokerConfig brokerProperties,
                   ConcurrentHashMap<String, ChannelId> channelIdMap,
                   ISessionStoreService sessionStoreService) {
        this.packetService = packetService;
        this.brokerProperties = brokerProperties;
        this.sessionStoreService = sessionStoreService;
        this.channelIdMap = channelIdMap;
    }

    public void processPingReq(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();

        LOGGER.info("PINGREQ - from clientId: {} heartbeat", clientId);
        packetService.Log("PINGREQ", clientId, null, "[C -> S] heartbeat", MqttQoS.AT_MOST_ONCE.toString());

        if (sessionStoreService.containsKey(clientId)) {
            SessionStore sessionStore = sessionStoreService.get(clientId);
            ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());

            MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    null, null);
            if (brokerProperties.getId().equals(sessionStore.getBrokerId()) && channelId != null) {
                sessionStoreService.expire(clientId, sessionStore.getExpire());
            }

            LOGGER.info("PINGRESP - to clientId: {}", clientId);
            packetService.Log("PINGRESP", clientId, null, "[C <- S] heartbeat", MqttQoS.AT_MOST_ONCE.toString());
            channel.writeAndFlush(pingRespMessage);
        }
    }
}
