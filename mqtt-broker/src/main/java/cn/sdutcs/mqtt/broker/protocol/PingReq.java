package cn.sdutcs.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PINGREQ连接处理
 */
public class PingReq {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingReq.class);

    public void processPingReq(Channel channel, MqttMessage msg) {

    }
}
