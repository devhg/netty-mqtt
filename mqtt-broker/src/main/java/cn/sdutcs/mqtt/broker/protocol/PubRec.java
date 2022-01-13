package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.common.message.DupPubRelMessageStore;
import cn.sdutcs.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.sdutcs.mqtt.common.message.IDupPublishMessageStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PUBREC 连接处理，消息已接收(Qos2第一阶段)
 */
public class PubRec {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

    private final IDupPublishMessageStoreService dupPublishMessageStoreService;
    private final IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    public PubRec(IDupPublishMessageStoreService dupPublishMessageStoreService,
                  IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
    }

    public void processPubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = variableHeader.messageId();

        // 1. 删掉 PublishMessage
        dupPublishMessageStoreService.remove(clientId, messageId);
        LOGGER.debug("PUBREC - clientId: {}, messageId: {}", clientId, messageId);
        // 2. 发送 PubRelMessage
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
        DupPubRelMessageStore dupPubRelMessage = new DupPubRelMessageStore(clientId, messageId);
        dupPubRelMessageStoreService.put(clientId, dupPubRelMessage);
        channel.writeAndFlush(dupPubRelMessage);
    }
}
