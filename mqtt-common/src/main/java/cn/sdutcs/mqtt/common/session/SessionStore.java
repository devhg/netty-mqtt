package cn.sdutcs.mqtt.common.session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 会话存储
 */
public class SessionStore implements Serializable {

    private static final long serialVersionUID = 2139880735063574235L;

    private String brokerId;

    private String clientId;

    private String channelId;

    private int expire;

    private boolean cleanSession;

    private MqttPublishMessage willMessage;

    public SessionStore() {

    }

    public SessionStore(String brokerId, String clientId, String channelId, boolean cleanSession, MqttPublishMessage willMessage, int expire) {
        this.brokerId = brokerId;
        this.clientId = clientId;
        this.channelId = channelId;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
        this.expire = expire;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public SessionStore setBrokerId(String brokerId) {
        this.brokerId = brokerId;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public SessionStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public SessionStore setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public int getExpire() {
        return expire;
    }

    public SessionStore setExpire(int expire) {
        this.expire = expire;
        return this;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public SessionStore setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    public MqttPublishMessage getWillMessage() {
        return willMessage;
    }

    public SessionStore setWillMessage(MqttPublishMessage willMessage) {
        this.willMessage = willMessage;
        return this;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("clientId", clientId);
        sessionMap.put("channelId", channelId);
        sessionMap.put("cleanSession", cleanSession);
        sessionMap.put("brokerId", brokerId);
        sessionMap.put("expire", expire);
        if (willMessage != null) {
            sessionMap.put("payload", Base64.getEncoder().encodeToString(willMessage.payload().array()));
            sessionMap.put("messageType", willMessage.fixedHeader().messageType().value());
            sessionMap.put("isDup", willMessage.fixedHeader().isDup());
            sessionMap.put("qosLevel", willMessage.fixedHeader().qosLevel().value());
            sessionMap.put("isRetain", willMessage.fixedHeader().isRetain());
            sessionMap.put("remainingLength", willMessage.fixedHeader().remainingLength());
            sessionMap.put("topicName", willMessage.variableHeader().topicName());
            sessionMap.put("packetId", willMessage.variableHeader().packetId());
            sessionMap.put("hasWillMessage", true);
        }
        return sessionMap;
    }

    /**
     * 从map转为SessionStore对象
     */
    public static SessionStore ValueOf(Map<String, Object> sessionMap) {
        SessionStore sessionStore = new SessionStore();
        if (sessionMap.containsKey("hasWillMessage")) {
            byte[] payloads = Base64.getDecoder().decode((String) sessionMap.get("payload"));
            ByteBuf buf = null;
            try {
                buf = Unpooled.wrappedBuffer(payloads);
                MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                        MqttMessageType.valueOf((Integer) sessionMap.get("messageType")),
                        (Boolean) sessionMap.get("isDup"),
                        MqttQoS.valueOf((Integer) sessionMap.get("qosLevel")),
                        (Boolean) sessionMap.get("isRetain"),
                        (Integer) sessionMap.get("remainingLength"));

                MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(
                        (String) sessionMap.get("topicName"),
                        (Integer) sessionMap.get("packetId"));
                MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, buf);
                sessionStore.setWillMessage(mqttPublishMessage);
            } finally {
                if (buf != null) {
                    buf.release();
                }
            }
        }

        String clientId = (String) sessionMap.get("clientId");
        String channelId = (String) sessionMap.get("channelId");
        Boolean cleanSession = (Boolean) sessionMap.get("cleanSession");
        String brokerId = (String) sessionMap.get("brokerId");
        Integer expire = (Integer) sessionMap.get("expire");

        sessionStore.setChannelId(clientId).setChannelId(channelId).setCleanSession(cleanSession).
                setBrokerId(brokerId).setExpire(expire);
        return null;
    }
}
