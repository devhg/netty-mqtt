package cn.sdutcs.mqtt.panel.model;

import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Topic {
    private int id;
    private String type;
    private String topic;
    private Long count;
    private Date updateTime;
    private List<SubscribeStore> subscribeStores;

    public Topic(int id, String type, String topic, Long count, Date updateTime, List<SubscribeStore> subscribeStores) {
        this.id = id;
        this.type = type;
        this.topic = topic;
        this.count = count;
        this.updateTime = updateTime;
        this.subscribeStores = subscribeStores;
    }
}
