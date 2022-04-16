package cn.sdutcs.mqtt.panel.service;

import cn.sdutcs.mqtt.panel.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        System.out.println(secondKey + " " + s);
        if (null == s) {
            return 0;
        } else {
            return Integer.valueOf(s);
        }
    }
}
