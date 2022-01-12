package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.service.IDupPubRelMessageStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBREL连接处理，消息释放(Qos2第二阶段)
 */
public class PubRel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

    public void processPubRel(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = variableHeader.messageId();
        MqttMessage pubCompMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
        LOGGER.debug("PUBREL - clientId: {}, messageId: {}", clientId, messageId);
        channel.writeAndFlush(pubCompMessage);
    }
}
