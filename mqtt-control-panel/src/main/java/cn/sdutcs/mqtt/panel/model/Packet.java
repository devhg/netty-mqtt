package cn.sdutcs.mqtt.panel.model;

import lombok.Data;

import java.util.Date;

@Data
public class Packet {
    Long packetId;
    String packetType;
    String clientId;
    String topic;
    String packetInfo;
    String qos;
    Date createTime;

    public Packet(String packetType, String clientId, String topic, String packetInfo, String qos) {
        this.packetType = packetType;
        this.clientId = clientId;
        this.topic = topic;
        this.packetInfo = packetInfo;
        this.qos = qos;
    }
}
