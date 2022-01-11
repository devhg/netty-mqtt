package cn.sdutcs.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBACK连接处理
 */
public class PubAck {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubAck.class);

    public void processPubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {

    }

}
