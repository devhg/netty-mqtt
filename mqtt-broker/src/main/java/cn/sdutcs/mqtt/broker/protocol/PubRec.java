package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.service.PacketService;
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

    private final PacketService packetService;
    private final IDupPublishMessageStoreService dupPublishMessageStoreService;
    private final IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    public PubRec(PacketService packetService,
                  IDupPublishMessageStoreService dupPublishMessageStoreService,
                  IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
        this.packetService = packetService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
    }

    public void processPubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        int messageId = variableHeader.messageId();

        // 1. 删掉 dupPublishMessage，存储 dupPubRelMessage
        dupPublishMessageStoreService.remove(clientId, messageId);
        DupPubRelMessageStore dupPubRelMessage = new DupPubRelMessageStore(clientId, messageId);
        dupPubRelMessageStoreService.put(clientId, dupPubRelMessage);

        LOGGER.info("PUBREC - from clientId: {}, messageId: {}", clientId, messageId);
        packetService.Log("PUBREC", clientId, null, "[C -> S] pubrec from client, messageId=" + messageId, MqttQoS.AT_MOST_ONCE.toString());

        // 2. 发送 PubRelMessage
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);

        LOGGER.info("PUBREL - to clientId: {}, messageId: {}", clientId, messageId);
        packetService.Log("PUBREL", clientId, null, "[C <- S] pubrel to client, messageId=" + messageId, MqttQoS.AT_MOST_ONCE.toString());

        channel.writeAndFlush(pubRelMessage);
    }
}
