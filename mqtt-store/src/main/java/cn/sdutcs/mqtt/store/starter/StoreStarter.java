package cn.sdutcs.mqtt.store.starter;

import lombok.Data;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Component
// @EnableConfigurationProperties({StoreStarter.class})
// @ConfigurationProperties(prefix = "mqtt.broker.kafka")
@Data
public class StoreStarter {
    protected KafkaProducer kafkaProducer;
    protected UnifiedJedis jedis;

    // @PropDoc(group = "broker", value = "实例名称", need = true, defaultValue = "mqtt")
    @Value("${mqtt.broker.id}")
    public String PROP_INSTANCENAME = "mqtt";

    // @PropDoc(group = "broker", value = "是否启用kafka消息转发", need = true, defaultValue = "false")
    @Value("${mqtt.broker.kafka.broker-enabled}")
    public Boolean PROP_KAFKA_BROKER_ENABLED;

    // @PropDoc(group = "broker", value = "kafka地址 127.0.0.1:9092,127.0.0.1:9093", need = true, defaultValue = "127.0.0.1:9092")
    @Value("${mqtt.broker.kafka.bootstrap.servers}")
    public String PROP_KAFKA_SERVERS;

    // @PropDoc(group = "broker", value = "all:必须等待回令 -1:不等待", defaultValue = "all")
    @Value("${mqtt.broker.kafka.acks}")
    public String PROP_KAFKA_ACKS;

    // @PropDoc(group = "broker", value = "重试次数", type = "int", defaultValue = "0")
    @Value("${mqtt.broker.kafka.retries}")
    public String PROP_KAFKA_RETRIES;

    // @PropDoc(group = "broker", value = "批量提交大小", type = "int", defaultValue = "16384")
    @Value("${mqtt.broker.kafka.batch.size}")
    public String PROP_KAFKA_BATCHSIZE;

    // @PropDoc(group = "broker", value = "提交延迟等待时间(等待时间内可以追加提交)", type = "int", defaultValue = "1")
    @Value("${mqtt.broker.kafka.linger.ms}")
    public String PROP_KAFKA_LINGERMS;

    // @PropDoc(group = "broker", value = "缓存大小(Bit) 默认:64MB", type = "int", defaultValue = "67108864")
    @Value("${mqtt.broker.kafka.buffer.memory}")
    public String PROP_KAFKA_BUFFERMEMORY;

    // @PropDoc(group = "broker", value = "key序列化方法", defaultValue = "org.apache.kafka.common.serialization.StringSerializer")
    @Value("${mqtt.broker.kafka.key.serializer}")
    public String PROP_KAFKA_KEYSERIALIZER;

    // @PropDoc(group = "broker", value = "value序列化方法", defaultValue = "org.apache.kafka.common.serialization.StringSerializer")
    @Value("${mqtt.broker.kafka.value.serializer}")
    public String PROP_KAFKA_VALUESERIALIZER;

    // @PropDoc(group = "broker", value = "分发策略", defaultValue = "cn.wizzer.iot.mqtt.server.store.kafka.SimplePartitioner")
    @Value("${mqtt.broker.kafka.partitioner.class}")
    public String PROP_KAFKA_PARTITIONERCLASS;

    // @PropDoc(group = "broker", value = "分发策略", defaultValue = "cn.wizzer.iot.mqtt.server.store.kafka.SimplePartitioner")
    @Value("${mqtt.broker.kafka.producer.topic}")
    public String PROP_KAFKA_TOPIC;

    @Value("${mqtt.broker.redis-enabled}")
    private Boolean redisEnabled;
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private Integer redisPort;
    @Value("${redis.timeout}")
    private Integer redisTimeout;
    @Value("${redis.database}")
    private String redisDatabase;
    @Value("${redis.maxTotal}")
    private Integer redisMaxTotal;
    @Value("${redis.pool.maxIdle}")
    private Integer redisPoolMaxIdle;
    @Value("${redis.pool.minIdle}")
    private Integer redisPoolMInIdle;
    @Value("${redis.mode}")
    private String redisMode;
    @Value("${redis.nodes}")
    private String redisEndpoints;

    public static boolean isClusterMode = false;

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", PROP_KAFKA_SERVERS);
        properties.put("acks", PROP_KAFKA_ACKS);
        properties.put("reties", PROP_KAFKA_RETRIES);
        properties.put("linger.ms", PROP_KAFKA_LINGERMS);
        properties.put("key.serializer", PROP_KAFKA_KEYSERIALIZER);
        properties.put("value.serializer", PROP_KAFKA_VALUESERIALIZER);
        properties.put("partitioner.class", PROP_KAFKA_PARTITIONERCLASS);
        properties.put("producer.topic", PROP_KAFKA_TOPIC);
        return properties;
    }

    @Bean
    public KafkaProducer kafkaProducer() {
        return this.kafkaProducer;
    }

    @Bean
    public UnifiedJedis jedisPooled() {
        return this.jedis;
    }

    @PostConstruct
    public void init() throws Exception {
        if (PROP_KAFKA_BROKER_ENABLED) {
            this.kafkaProducer = new KafkaProducer(getProperties());
        }
        if (redisEnabled) {
            if (redisMode.equals("normal")) {
                this.jedis = new JedisPooled(redisHost, redisPort);
            } else if (redisMode.equals("cluster")) {
                StoreStarter.isClusterMode = true;
                Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
                String[] nodes = redisEndpoints.split(",");
                Arrays.stream(nodes).forEach((addr) -> {
                    jedisClusterNodes.add(HostAndPort.from(addr));
                });
                this.jedis = new JedisCluster(jedisClusterNodes);
            }
        }
    }

    @PreDestroy
    public void close() throws Exception {
        if (this.kafkaProducer != null) {
            this.kafkaProducer.close();
        }
        if (this.jedis != null) {
            jedis.close();
        }
    }
}
