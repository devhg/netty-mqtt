package cn.sdutcs.mqtt.broker.protocol;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.domain.MqttMessageHelper;
import cn.sdutcs.mqtt.broker.internal.RemotingHelper;
import cn.sdutcs.mqtt.broker.service.BlackListService;
import cn.sdutcs.mqtt.common.auth.IAuthService;
import cn.sdutcs.mqtt.common.message.DupPubRelMessageStore;
import cn.sdutcs.mqtt.common.message.DupPublishMessageStore;
import cn.sdutcs.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.sdutcs.mqtt.common.message.IDupPublishMessageStoreService;
import cn.sdutcs.mqtt.common.session.ISessionStoreService;
import cn.sdutcs.mqtt.common.session.SessionStore;
import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CONNECT连接处理
 * 返回CONNACK
 */
public class Connect {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connect.class);

    private final BrokerConfig brokerProperties;
    private final BlackListService blackListService;
    private final IAuthService authService;
    private final ISessionStoreService sessionStoreService;
    private final ISubscribeStoreService subscribeStoreService;
    private final IDupPublishMessageStoreService dupPublishMessageStoreService;
    private final IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    private final ChannelGroup channelGroup;
    private final ConcurrentHashMap<String, ChannelId> channelIdMap;

    public Connect(BrokerConfig brokerProperties,
                   BlackListService blackListService,
                   IAuthService authService,
                   ISessionStoreService sessionStoreService,
                   ISubscribeStoreService subscribeStoreService,
                   IDupPublishMessageStoreService dupPublishMessageStoreService,
                   IDupPubRelMessageStoreService dupPubRelMessageStoreService,
                   ChannelGroup channelGroup,
                   ConcurrentHashMap<String, ChannelId> channelIdMap) {
        this.brokerProperties = brokerProperties;
        this.blackListService = blackListService;
        this.authService = authService;
        this.sessionStoreService = sessionStoreService;
        this.subscribeStoreService = subscribeStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
        this.channelGroup = channelGroup;
        this.channelIdMap = channelIdMap;
    }

    public void processConnect(Channel channel, MqttConnectMessage msg) {
        String clientId = msg.payload().clientIdentifier();
        String username = msg.payload().userName();
        String password = msg.payload().passwordInBytes() == null ? null : new String(msg.payload().passwordInBytes(), StandardCharsets.UTF_8);
        MqttQoS qoS = msg.fixedHeader().qosLevel();
        boolean msgIsCleanSession = msg.variableHeader().isCleanSession();

        LOGGER.info("CONNECT [C -> S] - from clientId: {}, cleanSession: {}, Qos: {}", clientId, msgIsCleanSession, qoS);

        boolean sessionPresent;
        MqttConnectReturnCode returnCode;

        if (StrUtil.isBlank(clientId)) {
            // clientId为空或null的情况, 这里要求客户端必须提供clientId, 不管cleanSession是否为1, 此处没有参考标准协议实现
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
            LOGGER.warn("CONNECT [rejected] - Blank clientIdentifier");
        } else if (blackListService.onBlackList(RemotingHelper.getRemoteAddr(channel))) {
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;
            LOGGER.warn("CONNECT [rejected] - IP onBlackList");
        } else if (brokerProperties.isMqttPasswordMust() && !authService.checkValid(username, password)) {
            // 用户名和密码验证, 这里要求客户端连接时必须提供用户名和密码, 不管是否设置用户名标志和密码标志为1, 此处没有参考标准协议实现
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
            LOGGER.warn("CONNECT [rejected] - clientId: {}, username: {} login with failed password", clientId, username);
        } else {
            returnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
        }

        if (returnCode != MqttConnectReturnCode.CONNECTION_ACCEPTED) {
            channel.writeAndFlush(MqttMessageHelper.getConnectAckMessage(returnCode, false));
            channel.close();
        }

        // 如果会话中已存储这个新连接的clientId, 需要开辟新的会话的话，就关闭之前该clientId的连接
        if (sessionStoreService.containsKey(clientId)) {
            SessionStore sessionStore = sessionStoreService.get(clientId);
            // 全新的会话
            if (sessionStore.isCleanSession()) {
                sessionStoreService.remove(clientId);
                subscribeStoreService.removeForClient(clientId);
                dupPublishMessageStoreService.removeByClient(clientId);
                dupPubRelMessageStoreService.removeByClient(clientId);
            }

            try {
                ChannelId channelId = channelIdMap.get(sessionStore.getBrokerId() + "_" + sessionStore.getChannelId());
                if (channelId != null) {
                    Channel previous = channelGroup.find(channelId);
                    if (previous != null) previous.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 如果不存在session，则清除之前的其他缓存
            subscribeStoreService.removeForClient(clientId);
            dupPublishMessageStoreService.removeByClient(clientId);
            dupPubRelMessageStoreService.removeByClient(clientId);
        }

        // 处理连接心跳包，连接alive时间，用客户端请求中设置的keepAliveTimeSeconds替换BrokerServer预配置的
        int expire = 0;
        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains("idle")) {
                channel.pipeline().remove("idle");
            }
            // 若Broker超过1.5T时间没收到心跳请求则断开连接，并且投递遗嘱消息到订阅方 todo 如何投递遗嘱
            expire = Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f);
            channel.pipeline().addFirst("idle", new IdleStateHandler(expire, 0, 0));
        }

        // 处理遗嘱信息
        SessionStore sessionStore = new SessionStore(brokerProperties.getId(), clientId, channel.id().asLongText(), msgIsCleanSession, null, expire);
        if (msg.variableHeader().isWillFlag()) {
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(), 0),
                    Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()));
            sessionStore.setWillMessage(willMessage);
        }

        // 存储此会话信息及返回接受客户端连接
        sessionStoreService.put(clientId, sessionStore, expire);
        // 将clientId存储到channel的map中
        channel.attr(AttributeKey.valueOf("clientId")).set(clientId);
        sessionPresent = sessionStoreService.containsKey(clientId) && !msgIsCleanSession;

        MqttConnAckMessage connAckMessage = MqttMessageHelper.getConnectAckMessage(returnCode, sessionPresent);
        LOGGER.info("CONNACK [C <- S] - to clientId: {}, cleanSession: {} established", clientId, msg.variableHeader().isCleanSession());
        channel.writeAndFlush(connAckMessage);

        // 如果cleanSession为0则表示复用之前的session, 需要重发此clientId存储的未完成的QoS1和QoS2的DUP消息
        if (!msg.variableHeader().isCleanSession()) {
            List<DupPublishMessageStore> dupPublishMessageStoreList = dupPublishMessageStoreService.get(clientId);
            List<DupPubRelMessageStore> dupPubRelMessageStoreList = dupPubRelMessageStoreService.get(clientId);

            dupPublishMessageStoreList.forEach(dupPublishMessageStore -> {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(dupPublishMessageStore.getMqttQoS()), false, 0),
                        new MqttPublishVariableHeader(dupPublishMessageStore.getTopic(), dupPublishMessageStore.getMessageId()),
                        Unpooled.buffer().writeBytes(dupPublishMessageStore.getMessageBytes()));
                channel.writeAndFlush(publishMessage);
            });

            dupPubRelMessageStoreList.forEach(dupPubRelMessageStore -> {
                MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                        MqttMessageIdVariableHeader.from(dupPubRelMessageStore.getMessageId()),
                        null);
                channel.writeAndFlush(pubRelMessage);
            });
        }
    }
}
