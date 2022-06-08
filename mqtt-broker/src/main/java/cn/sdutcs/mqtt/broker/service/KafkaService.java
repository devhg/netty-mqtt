package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.internal.InternalMessage;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.List;

@Service
public class KafkaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private BrokerConfig brokerProperties;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(InternalMessage internalMessage) {
        try {
            // 消息体转换为Hex字符串进行转发
            ProducerRecord<String, String> data = new ProducerRecord<>(brokerProperties.getKafkaProducerTopic(),
                    internalMessage.getTopic(), JSONObject.toJSONString(internalMessage));
            kafkaTemplate.send(data).addCallback(
                    new SuccessCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> sendResult) {
                            RecordMetadata meta = sendResult.getRecordMetadata();
                            LOGGER.info("[kafka] topic: {}, offset: {}, partition: {}", meta.topic(), meta.offset(), meta.partition());
                        }
                    },
                    new FailureCallback() {
                        @Override
                        public void onFailure(Throwable e) {
                            e.printStackTrace();
                            LOGGER.error(e.getMessage(), e);
                        }
                    });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static final String KAFKA_LISTENER_ID = "KAFKA_ID_1";

    // https://blog.csdn.net/H900302/article/details/110791942
    @KafkaListener(id = KAFKA_LISTENER_ID,
            topics = "${mqtt.broker.kafka.producer.topic}",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "false")
    public void listener(List<String> message) {

        // todo InternalMessage 转成 mqttMessage 并转发出去
        LOGGER.info("[kafka] receive: {}", message);
    }
}
