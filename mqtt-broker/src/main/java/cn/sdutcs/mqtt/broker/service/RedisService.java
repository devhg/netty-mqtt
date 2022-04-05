package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisService {

    @Autowired
    private BrokerConfig brokerConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 将broker配置信息存到redis中
     */
    public void storeBrokerConfig() {
        redisTemplate.opsForValue().set("broker_config", JSON.toJSONString(brokerConfig.getConfigMap()));
    }
}
