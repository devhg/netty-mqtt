package cn.sdutcs.mqtt.broker.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * CONNECT连接处理
 */
public class Connect {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connect.class);

    private ChannelGroup channelGroup;

    private Map<String, ChannelId> channelIdMap;

    public void processConnect(Channel channel, MqttConnectMessage msg) {

    }
}
