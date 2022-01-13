package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.common.message.IDupPubRelMessageStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBCOMP连接处理，发布结束(Qos2第三阶段)
 */
public class PubComp {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubComp.class);

    private final IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    public PubComp(IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
    }

    public void processPubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = variableHeader.messageId();
        LOGGER.debug("PUBCOMP - clientId: {}, messageId: {}", clientId, messageId);
        dupPubRelMessageStoreService.remove(clientId, messageId);
    }
}
