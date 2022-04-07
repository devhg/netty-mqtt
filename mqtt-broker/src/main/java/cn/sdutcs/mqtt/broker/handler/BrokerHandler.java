package cn.sdutcs.mqtt.broker.handler;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.protocol.ProtocolProcess;
import cn.sdutcs.mqtt.common.session.SessionStore;
import cn.sdutcs.mqtt.store.cache.QpsCounter;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.ssl.NotSslRecordException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT消息处理
 */
@Component
@ChannelHandler.Sharable
public class BrokerHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerHandler.class);

    @Autowired
    private ProtocolProcess protocolProcess;

    @Autowired
    private BrokerConfig brokerProperties;

    @Autowired
    private ChannelGroup channelGroup;

    @Autowired
    private ConcurrentHashMap<String, ChannelId> channelIdMap;

    @Autowired
    QpsCounter qpsCounter;

    @Autowired
    private CountInfo countInfo;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channelGroup.add(ctx.channel());
        this.channelIdMap.put(brokerProperties.getId() + "_" + ctx.channel().id().asLongText(), ctx.channel().id());
        countInfo.setCurChannelNum(channelGroup.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.channelGroup.remove(ctx.channel());
        this.channelIdMap.remove(brokerProperties.getId() + "_" + ctx.channel().id().asLongText());
        System.out.println("inactive");
        countInfo.setCurChannelNum(channelGroup.size());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        // qps计数
        // qpsCounter.Count("qps");
        countInfo.getReceiveNum().incrementAndGet();
        countInfo.setLastReceiveTime(System.currentTimeMillis());

        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                // 不支持的协议版本
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false),
                        null);
                ctx.writeAndFlush(connAckMessage);
            } else if (cause instanceof MqttIdentifierRejectedException) {
                // 不合格的clientId
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false),
                        null);
                ctx.writeAndFlush(connAckMessage);
            }
            ctx.close();
            return;
        }

        switch (msg.fixedHeader().messageType()) {
            case CONNECT:
                protocolProcess.connect().processConnect(ctx.channel(), (MqttConnectMessage) msg);
                break;
            case CONNACK:
                break;
            case PUBLISH:
                protocolProcess.publish().processPublish(ctx.channel(), (MqttPublishMessage) msg);
                break;
            case PUBACK:
                protocolProcess.pubAck().processPubAck(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;
            case PUBREC:
                protocolProcess.pubRec().processPubRec(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;
            case PUBREL:
                protocolProcess.pubRel().processPubRel(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;
            case PUBCOMP:
                protocolProcess.pubComp().processPubComp(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                break;
            case SUBSCRIBE:
                protocolProcess.subscribe().processSubscribe(ctx.channel(), (MqttSubscribeMessage) msg);
                break;
            case SUBACK:
                break;
            case UNSUBSCRIBE:
                protocolProcess.unSubscribe().processUnSubscribe(ctx.channel(), (MqttUnsubscribeMessage) msg);
                break;
            case UNSUBACK:
                break;
            case PINGREQ:
                protocolProcess.pingReq().processPingReq(ctx.channel(), msg);
                break;
            case PINGRESP:
                break;
            case DISCONNECT:
                protocolProcess.disConnect().processDisConnect(ctx.channel(), msg);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else if (cause instanceof DecoderException) {
            if (cause.getCause() instanceof NotSslRecordException) {
                // 未通过SSL认证
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false),
                        null);
                ctx.writeAndFlush(connAckMessage);
                LOGGER.error("SSL verify ERROR");
            }
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * 用于心跳超时，处理遗嘱消息
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            // 读超时
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
                // 发送遗嘱消息
                if (this.protocolProcess.getSessionStoreService().containsKey(clientId)) {
                    SessionStore sessionStore = this.protocolProcess.getSessionStoreService().get(clientId);
                    if (sessionStore.getWillMessage() != null) {
                        this.protocolProcess.publish().processPublish(ctx.channel(), sessionStore.getWillMessage());
                    }
                }
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
