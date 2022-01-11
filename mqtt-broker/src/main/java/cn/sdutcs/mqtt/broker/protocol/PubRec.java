package cn.sdutcs.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBREC连接处理
 */
public class PubRec {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

    public void processPubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {

    }
}
