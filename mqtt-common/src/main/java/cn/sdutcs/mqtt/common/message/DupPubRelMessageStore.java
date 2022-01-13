package cn.sdutcs.mqtt.common.message;

import java.io.Serializable;

/**
 * PUBREL重发消息存储
 */
public class DupPubRelMessageStore implements Serializable {

    private static final long serialVersionUID = -4811909730136687408L;

    private String clientId;

    private int messageId;

    public DupPubRelMessageStore(String clientId, int messageId) {
        this.clientId = clientId;
        this.messageId = messageId;
    }

    public String getClientId() {
        return clientId;
    }

    public DupPubRelMessageStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public int getMessageId() {
        return messageId;
    }

    public DupPubRelMessageStore setMessageId(int messageId) {
        this.messageId = messageId;
        return this;
    }
}
