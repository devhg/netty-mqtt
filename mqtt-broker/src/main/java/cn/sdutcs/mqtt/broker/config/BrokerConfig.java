package cn.sdutcs.mqtt.broker.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("cn.sdutcs.mqtt.broker")
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "mqtt.broker")
@Data
public class BrokerConfig {

    /**
     * Broker唯一标识, 默认mqtt
     */
    @Value("${mqtt.broker.id:default-mqtt}")
    private String id;

    @Value("${mqtt.broker.host}")
    private String host;

    @Value("${mqtt.broker.port}")
    private Integer port;

    @Value("${mqtt.broker.mqtt-passwd-must:false}")
    private boolean mqttPasswordMust;

    @Value("${mqtt.broker.ssl-enabled:false}")
    private Boolean sslEnabled;

    @Value("${mqtt.broker.ssl-password}")
    private String sslPassword;

    @Value("${mqtt.broker.websocket-enabled:false}")
    private Boolean wsEnabled;

    @Value("${mqtt.broker.websocket-port}")
    private Integer wsPort;

    @Value("${mqtt.broker.websocket-path}")
    private String websocketPath;

    @Value("${mqtt.broker.keep-alive}")
    private int keepAlive;

    /**
     * Socket参数, 存放已完成三次握手请求的队列最大长度, 默认511长度
     */
    @Value("${mqtt.broker.so-backlog}")
    private Integer soBacklog;

    /**
     * Socket参数, 是否开启心跳保活机制, 默认开启
     */
    @Value("${mqtt.broker.so-keep-alive}")
    private Boolean soKeepAlive;
}
