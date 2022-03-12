package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.domain.MqttMessageHelper;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBREL连接处理，消息释放(Qos2第二阶段)
 * 1. 消息所有者转换为broker,开始分发消息
 * 2. 返回pubCom报文
 */
public class PubRel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

    public void processPubRel(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = variableHeader.messageId();
        LOGGER.info("PUBREL [C -> S] - clientId: {}, messageId: {}", clientId, messageId);

        MqttMessage pubComMessage = MqttMessageHelper.getPubComMessage(messageId);
        channel.writeAndFlush(pubComMessage);
    }
}
