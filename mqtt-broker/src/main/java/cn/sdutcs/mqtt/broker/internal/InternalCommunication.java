package cn.sdutcs.mqtt.broker.internal;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息转发，基于kafka
 */
@Component
public class InternalCommunication {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalCommunication.class);
    @Autowired
    private BrokerConfig brokerProperties;

    // @Autowired
    // private KafkaService kafkaService;
    // @Autowired
    // private RedisCluster redisCluster;

    public void internalSend(InternalMessage internalMessage) {
        // String processId = Lang.JdkTool.getProcessId("0");
        // //broker唯一标识 mqttwk.broker.id
        // internalMessage.setBrokerId(brokerProperties.getId());
        // internalMessage.setProcessId(processId);
        // //如果开启kafka消息转发
        // if (brokerProperties.getKafkaBrokerEnabled()) {
        //     kafkaService.send(internalMessage);
        // }
        // //如果开启集群功能
        // if (brokerProperties.getClusterEnabled()) {
        //     redisCluster.sendMessage(internalMessage);
        // }
    }
}
