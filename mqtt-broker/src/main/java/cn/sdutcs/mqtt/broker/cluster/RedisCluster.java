package cn.sdutcs.mqtt.broker.cluster;

import cn.sdutcs.mqtt.broker.internal.InternalMessage;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisCluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCluster.class);

    // private static final String CLUSTER_TOPIC = "mqttwk:cluster";
    // private BrokerConfig brokerProperties;
    // private InternalSendServer internalSendServer;
    //
    // public void init() {
    //     pubSubService.reg(CLUSTER_TOPIC, this);
    // }
    //
    // @Override
    // public void onMessage(String channel, String message) {
    //     InternalMessage internalMessage = JSONObject.parseObject(message, InternalMessage.class);
    //     //判断进程ID是否是自身实例,若相同则不发送,否则集群模式下重复发消息
    //     if (!brokerProperties.getId().equals(internalMessage.getBrokerId()) && !Lang.JdkTool.getProcessId("0").equals(internalMessage.getProcessId()))
    //         internalSendServer.sendPublishMessage(internalMessage.getClientId(), internalMessage.getTopic(), MqttQoS.valueOf(internalMessage.getMqttQoS()), internalMessage.getMessageBytes(), internalMessage.isRetain(), internalMessage.isDup());
    // }
    //
    public void sendMessage(InternalMessage internalMessage) {
        // pubSubService.fire(CLUSTER_TOPIC, JSONObject.toJSONString(internalMessage));
        LOGGER.info("{} -> {}", "CLUSTER_TOPIC", JSONObject.toJSONString(internalMessage));
    }

}
