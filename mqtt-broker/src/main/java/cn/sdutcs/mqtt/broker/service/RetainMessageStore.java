package cn.sdutcs.mqtt.broker.service;

import java.io.Serializable;

/**
 * Retain标志消息存储
 */
public class RetainMessageStore implements Serializable {

    private static final long serialVersionUID = 6301932616026732365L;

    private String topic;

    private byte[] messageBytes;

    private int mqttQoS;

    public String getTopic() {
        return topic;
    }

    public RetainMessageStore setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public RetainMessageStore setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
        return this;
    }

    public int getMqttQoS() {
        return mqttQoS;
    }

    public RetainMessageStore setMqttQoS(int mqttQoS) {
        this.mqttQoS = mqttQoS;
        return this;
    }
}
