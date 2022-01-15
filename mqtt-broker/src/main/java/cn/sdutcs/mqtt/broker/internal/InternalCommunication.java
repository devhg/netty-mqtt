package cn.sdutcs.mqtt.broker.internal;

import cn.sdutcs.mqtt.broker.cluster.RedisCluster;
import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.service.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

/**
 * 消息转发，基于kafka
 */
@Component
public class InternalCommunication {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalCommunication.class);
    @Autowired
    private BrokerConfig brokerProperties;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private RedisCluster redisCluster;

    public void internalSend(InternalMessage internalMessage) {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];

        // broker唯一标识 mqttwk.broker.id
        internalMessage.setBrokerId(brokerProperties.getId());
        internalMessage.setProcessId(pid);
        // 如果开启kafka消息转发
        if (brokerProperties.isKafkaBrokerEnabled()) {
            kafkaService.send(internalMessage);
        }
        // 如果开启集群功能
        if (brokerProperties.isClusterEnabled()) {
            redisCluster.sendMessage(internalMessage);
        }

        LOGGER.info("消息转发 {}", internalMessage);
    }
}
