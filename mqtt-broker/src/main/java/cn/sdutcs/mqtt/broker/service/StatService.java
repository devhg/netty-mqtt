package cn.sdutcs.mqtt.broker.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.handler.CountInfo;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;

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
     * 将连接数等统计信息存到redis中，保存最近七天的数据
     */
    public void storeBrokerCountInfo(CountInfo countInfo) {
        OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
        double cpu = osMxBean.getSystemLoadAverage();
        countInfo.setCpu(cpu);

        String today = DateUtil.today();
        redisTemplate.opsForValue().set("broker_counter:" + today, JSON.toJSONString(countInfo), 10, TimeUnit.DAYS);
    }

    /**
     * 将当日的连接数等统计信息加载到redis中，保存最近七天的数据
     */
    @Bean
    public CountInfo loadBrokerCountInfo() {
        String today = DateUtil.today();
        String s = redisTemplate.opsForValue().get("broker_counter:" + today);
        if (!StrUtil.isBlank(s)) {
            return JSON.parseObject(s, CountInfo.class);
        }
        CountInfo countInfo = new CountInfo();
        countInfo.setLastSentTime(System.currentTimeMillis());
        countInfo.setLastReceiveTime(System.currentTimeMillis());
        return countInfo;
    }
}
