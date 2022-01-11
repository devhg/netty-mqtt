package cn.sdutcs.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UNSUBSCRIBE连接处理
 */
public class UnSubscribe {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnSubscribe.class);


    public void processUnSubscribe(Channel channel, MqttUnsubscribeMessage msg) {

    }
}
