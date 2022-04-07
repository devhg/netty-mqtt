package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.handler.CountInfo;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatService {

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

    /**
     * 将连接数等统计信息存到redis中
     */
    public void storeBrokerCountInfo(CountInfo countInfo) {
        redisTemplate.opsForValue().set("broker_counter", JSON.toJSONString(countInfo));
    }
}
