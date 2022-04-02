package cn.sdutcs.mqtt.broker.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

@Configuration
@EnableKafka
public class KafkaConsumerConfiguration {
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfiguration.class);

    // 监听器工厂
    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    // @Bean
    // public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
    //     ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    //     factory.setConsumerFactory(consumerFactory);
    //     factory.setConcurrency(1);
    //     factory.getContainerProperties().setPollTimeout(1500);
    //     factory.setBatchListener(true);
    //     factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    //
    //     return factory;
    // }

    @Autowired
    private BrokerConfig brokerConfig;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.setBatchListener(true);
        // 被过滤的消息将被丢弃
        factory.setAckDiscarded(true);
        // 设置记录筛选策略
        factory.setRecordFilterStrategy(new RecordFilterStrategy() {
            @Override
            public boolean filter(ConsumerRecord consumerRecord) {
                String msg = consumerRecord.value().toString();
                String brokerId = String.format("\"brokerId\":\"%s\"", brokerConfig.getId());
                // 返回true消息将会被丢弃。如果是本机发到kafka-bus的返回true丢弃
                if (msg.contains(brokerId)) {
                    System.out.println(msg + " 丢弃 @" + brokerId);
                    return true;
                }
                return false;
            }
        });
        return factory;
    }
}