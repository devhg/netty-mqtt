package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * UNSUBSCRIBE连接处理
 */
public class UnSubscribe {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnSubscribe.class);

    private ISubscribeStoreService subscribeStoreService;

    public UnSubscribe(ISubscribeStoreService subscribeStoreService) {
        this.subscribeStoreService = subscribeStoreService;
    }

    public void processUnSubscribe(Channel channel, MqttUnsubscribeMessage msg) {
        List<String> topicFilters = msg.payload().topics();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        topicFilters.forEach(topicFilter -> {
            subscribeStoreService.remove(topicFilter, clientId);
            LOGGER.info("UNSUBSCRIBE - clientId: {}, topicFilter: {}", clientId, topicFilter);
        });
        MqttUnsubAckMessage unsubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                null);
        channel.writeAndFlush(unsubAckMessage);
    }
}
