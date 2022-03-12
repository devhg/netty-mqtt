package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.domain.MqttMessageHelper;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(PubRec.class);

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

        LOGGER.info("PUBREC [C -> S] - from clientId: {}, messageId: {}", clientId, messageId);

        // 1. 删掉 dupPublishMessage，存储 dupPubRelMessage
        dupPublishMessageStoreService.remove(clientId, messageId);
        DupPubRelMessageStore dupPubRelMessage = new DupPubRelMessageStore(clientId, messageId);
        dupPubRelMessageStoreService.put(clientId, dupPubRelMessage);

        // 2. 发送 PubRelMessage
        MqttMessage pubRelMessage = MqttMessageHelper.getPubRelMessage(messageId);
        LOGGER.info("PUBREL [C <- S] - to clientId: {}, messageId: {}", clientId, messageId);
        channel.writeAndFlush(pubRelMessage);
    }
}
