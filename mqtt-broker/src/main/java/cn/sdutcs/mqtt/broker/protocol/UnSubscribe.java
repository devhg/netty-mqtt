package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.domain.MqttMessageHelper;
import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * UNSUBSCRIBE连接处理
 * 返回UNSUBACK
 */
public class UnSubscribe {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnSubscribe.class);

    private final ISubscribeStoreService subscribeStoreService;

    public UnSubscribe(ISubscribeStoreService subscribeStoreService) {
        this.subscribeStoreService = subscribeStoreService;
    }

    public void processUnSubscribe(Channel channel, MqttUnsubscribeMessage msg) {
        List<String> topicFilters = msg.payload().topics();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        topicFilters.forEach(topicFilter -> {
            subscribeStoreService.remove(topicFilter, clientId);
            LOGGER.info("UNSUBSCRIBE [C -> S] - from clientId: {}, topicFilter: {}", clientId, topicFilter);
        });

        MqttUnsubAckMessage unSubAckMessage = MqttMessageHelper.getUnSubAckMessage(msg.variableHeader().messageId());
        channel.writeAndFlush(unSubAckMessage);
    }
}
