package cn.sdutcs.mqtt.broker.service;

import io.netty.handler.codec.mqtt.MqttPublishMessage;

import java.io.Serializable;

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
}
