package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.common.message.IDupPublishMessageStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBACK连接处理
 */
public class PubAck {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubAck.class);

    private final IDupPublishMessageStoreService dupPublishMessageStoreService;

    public PubAck(IDupPublishMessageStoreService dupPublishMessageStoreService) {
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
    }

    public void processPubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = variableHeader.messageId();
        LOGGER.debug("PUBACK - clientId: {}, messageId: {}", clientId, messageId);
        dupPublishMessageStoreService.remove(clientId, messageId);
    }
}
