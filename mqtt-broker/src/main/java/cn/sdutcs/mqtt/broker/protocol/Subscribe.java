package cn.sdutcs.mqtt.broker.protocol;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.broker.domain.DeviceMessage;
import cn.sdutcs.mqtt.broker.domain.DeviceMessageBuilder;
import cn.sdutcs.mqtt.broker.domain.MqttMessageHelper;
import cn.sdutcs.mqtt.common.message.IMessageIdService;
import cn.sdutcs.mqtt.common.message.IRetainMessageStoreService;
import cn.sdutcs.mqtt.common.message.RetainMessageStore;
import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * SUBSCRIBE连接处理
 */
public class Subscribe {

    private static final Logger LOGGER = LoggerFactory.getLogger(Subscribe.class);

    private final IMessageIdService messageIdService;
    private final IRetainMessageStoreService retainMessageStoreService;
    private final ISubscribeStoreService subscribeStoreService;

    public Subscribe(IMessageIdService messageIdService,
                     IRetainMessageStoreService retainMessageStoreService,
                     ISubscribeStoreService subscribeStoreService) {
        this.messageIdService = messageIdService;
        this.retainMessageStoreService = retainMessageStoreService;
        this.subscribeStoreService = subscribeStoreService;
    }

    public void processSubscribe(Channel channel, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        if (this.validTopicFilter(topicSubscriptions)) {
            String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            List<Integer> mqttQoSList = new ArrayList<Integer>();
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                SubscribeStore subscribeStore = new SubscribeStore(clientId, topicFilter, mqttQoS.value());
                subscribeStoreService.put(topicFilter, subscribeStore);
                mqttQoSList.add(mqttQoS.value());

                LOGGER.info("SUBSCRIBE - from clientId: {}, topFilter: {}, QoS: {}", clientId, topicFilter, mqttQoS.value());
            });

            MqttMessage subAckMessage = MqttMessageHelper.getSubAckMessage(msg.variableHeader().messageId(), mqttQoSList);
            channel.writeAndFlush(subAckMessage);

            // 发布保留消息
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                this.sendRetainMessage(channel, topicFilter, mqttQoS);
            });
        } else {
            LOGGER.error("{} topics have not valid topic", topicSubscriptions);
            channel.close();
        }
    }

    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            // 以#或+符号开头的、以/符号结尾的订阅按非法订阅处理, 这里没有参考标准协议
            if (StrUtil.startWith(topicFilter, '+') || StrUtil.endWith(topicFilter, '/'))
                return false;
            if (StrUtil.contains(topicFilter, '#')) {
                // 如果出现多个#符号的订阅按非法订阅处理
                if (StrUtil.count(topicFilter, '#') > 1) return false;
            }
            if (StrUtil.contains(topicFilter, '+')) {
                //如果+符号和/+字符串出现的次数不等的情况按非法订阅处理
                if (StrUtil.count(topicFilter, '+') != StrUtil.count(topicFilter, "/+")) return false;
            }
        }
        return true;
    }

    private void sendRetainMessage(Channel channel, String topicFilter, MqttQoS mqttQoS) {
        List<RetainMessageStore> retainMessageStores = retainMessageStoreService.search(topicFilter);
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();

        retainMessageStores.forEach(retainMessageStore -> {
            MqttQoS respQoS = retainMessageStore.getMqttQoS() > mqttQoS.value() ? mqttQoS : MqttQoS.valueOf(retainMessageStore.getMqttQoS());
            String topicName = retainMessageStore.getTopic();
            if (respQoS == MqttQoS.AT_MOST_ONCE) {

                DeviceMessage deviceMessage = DeviceMessageBuilder.buildDeviceMessage(false, false, respQoS.value(), topicName, retainMessageStore.getMessageBytes());
                MqttPublishMessage pubMessage = MqttMessageHelper.getPubMessage(deviceMessage, 0);

                // MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                //         new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                //         new MqttPublishVariableHeader(retainMessageStore.getTopic(), 0),
                //         Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));

                LOGGER.info("PUBLISH [C <- S] - to clientId: {} send remain message. topic: {}, Qos: {}", clientId, topicName, respQoS.value());
                channel.writeAndFlush(pubMessage);
            }
            if (respQoS == MqttQoS.AT_LEAST_ONCE || respQoS == MqttQoS.EXACTLY_ONCE) {
                int messageId = messageIdService.getNextMessageId();
                DeviceMessage deviceMessage = DeviceMessageBuilder.buildDeviceMessage(false, false, respQoS.value(), topicName, retainMessageStore.getMessageBytes());
                MqttPublishMessage pubMessage = MqttMessageHelper.getPubMessage(deviceMessage, messageId);

                // MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                //         new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                //         new MqttPublishVariableHeader(retainMessageStore.getTopic(), messageId),
                //         Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));

                LOGGER.info("PUBLISH [C <- S] - to clientId: {} send remain message. topic: {}, Qos: {}, messageId: {}", clientId, retainMessageStore.getTopic(), respQoS.value(), messageId);
                channel.writeAndFlush(pubMessage);
            }
        });
    }
}
