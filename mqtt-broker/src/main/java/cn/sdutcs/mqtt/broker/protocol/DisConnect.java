package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.sdutcs.mqtt.common.message.IDupPublishMessageStoreService;
import cn.sdutcs.mqtt.common.session.ISessionStoreService;
import cn.sdutcs.mqtt.common.session.SessionStore;
import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DISCONNECT连接处理
 */
public class DisConnect {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisConnect.class);

    private final ISessionStoreService sessionStoreService;
    private final ISubscribeStoreService subscribeStoreService;
    private final IDupPublishMessageStoreService dupPublishMessageStoreService;
    private final IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    public DisConnect(ISessionStoreService sessionStoreService,
                      ISubscribeStoreService subscribeStoreService,
                      IDupPublishMessageStoreService dupPublishMessageStoreService,
                      IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
    }

    public void processDisConnect(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        SessionStore sessionStore = sessionStoreService.get(clientId);
        if (sessionStore != null && sessionStore.isCleanSession()) {
            subscribeStoreService.removeForClient(clientId);
            dupPublishMessageStoreService.removeByClient(clientId);
            dupPubRelMessageStoreService.removeByClient(clientId);
        }
        if (sessionStore != null) {
            // 处理遗嘱消息
            MqttPublishMessage willMessage = sessionStore.getWillMessage();
            if (willMessage != null) {
                channel.writeAndFlush(willMessage);
            }
            LOGGER.debug("DISCONNECT - clientId: {}, cleanSession: {}", clientId, sessionStore.isCleanSession());
            sessionStoreService.remove(clientId);
        }
        channel.close();
    }
}
