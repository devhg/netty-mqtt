package cn.sdutcs.mqtt.store.starter;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
// @EnableConfigurationProperties({StoreStarter.class})
// @ConfigurationProperties(prefix = "mqtt.broker.kafka")
@Data
public class StoreStarter {

    @Value("${mqtt.broker.id}")
    public String PROP_INSTANCE_NAME = "mqtt";

    @Value("${mqtt.broker.kafka.broker-enabled}")
    public Boolean PROP_KAFKA_BROKER_ENABLED;
}
