package cn.sdutcs.mqtt.store.cache;

import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.UnifiedJedis;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Repository
public class QpsCounter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Qps 计数
     */
    public void Count(String key) {
        String now = DateUtil.now();
        String secondKey = String.format("counter:%s:%s", key, now);
        Long count = redisTemplate.opsForValue().increment(secondKey);
        if (count != null && 1 == count) {
            redisTemplate.expire(secondKey, 300, TimeUnit.SECONDS);
        }
    }

    /**
     * 获取 time 秒的 Request 数目
     */
    public Integer GetCount(String key, String time) {
        Map<Date, Integer> counts = new HashMap<>();
        String secondKey = String.format("counter:%s:%s", key, time);
        String s = redisTemplate.opsForValue().get(secondKey);
        if (null == s) {
            return 0;
        } else {
            return Integer.valueOf(s);
        }
    }
}
