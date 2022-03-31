package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.internal.InternalMessage;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KafkaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);
    @Autowired(required = false)
    private KafkaProducer kafkaProducer;
    @Autowired
    private BrokerConfig brokerProperties;

    @SuppressWarnings("unchecked")
    public void send(InternalMessage internalMessage) {
        try {
            // 消息体转换为Hex字符串进行转发
            ProducerRecord<String, String> data = new ProducerRecord<>(brokerProperties.getKafkaProducerTopic(),
                    internalMessage.getTopic(), JSONObject.toJSONString(internalMessage));
            kafkaProducer.send(data, new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                        LOGGER.error(e.getMessage(), e);
                    } else {
                        LOGGER.info("The offset of the record we just sent is: " + metadata.offset());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("kafka没有连接成功..");
        }
    }
}
