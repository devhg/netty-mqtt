package cn.sdutcs.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBCOMP连接处理
 */
public class PubComp {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubComp.class);


    public void processPubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {

    }
}
