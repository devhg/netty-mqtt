package cn.sdutcs.mqtt.broker.service;

import java.util.List;

/**
 * PUBLISH重发消息存储服务接口, 当QoS=1和QoS=2时存在该重发机制
 */
public interface IDupPublishMessageStoreService {

    /**
     * 存储消息
     */
    void put(String clientId, DupPublishMessageStore dupPublishMessageStore);

    /**
     * 获取消息集合
     */
    List<DupPublishMessageStore> get(String clientId);

    /**
     * 删除消息
     */
    void remove(String clientId, int messageId);

    /**
     * 删除消息
     */
    void removeByClient(String clientId);

}
