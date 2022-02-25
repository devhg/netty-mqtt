package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.service.PacketService;
import cn.sdutcs.mqtt.common.message.IDupPubRelMessageStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBCOMP连接处理，发布结束(Qos2第三阶段)
 */
public class PubComp {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubComp.class);

    private final PacketService packetService;
    private final IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    public PubComp(PacketService packetService,
                   IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
        this.packetService = packetService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
    }

    public void processPubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = variableHeader.messageId();
        dupPubRelMessageStoreService.remove(clientId, messageId);
        LOGGER.info("PUBCOMP - from clientId: {}, messageId: {}", clientId, messageId);
        packetService.Log("PUBCOMP", clientId, null, "[C -> S] pub completed", MqttQoS.AT_MOST_ONCE.toString());
    }
}
