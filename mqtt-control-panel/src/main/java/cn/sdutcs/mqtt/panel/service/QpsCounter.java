package cn.sdutcs.mqtt.panel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Repository
public class QpsCounter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
