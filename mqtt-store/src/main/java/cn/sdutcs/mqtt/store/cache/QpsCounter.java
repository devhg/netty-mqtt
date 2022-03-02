package cn.sdutcs.mqtt.store.cache;

import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.UnifiedJedis;

import java.util.*;

@Repository
public class QpsCounter {
    @Autowired
    private UnifiedJedis redisService;

    /**
     * Qps 计数
     */
    public void Count(String key) {
        String now = DateUtil.now();
        String secondKey = String.format("counter:%s:%s", key, now);
        long count = redisService.incr(secondKey);
        if (1 == count) {
            redisService.expire(secondKey, 300);
        }
    }

    /**
     * 获取 time 秒的 Request 数目
     */
    public Integer GetCount(String key, String time) {
        Map<Date, Integer> counts = new HashMap<>();
        String secondKey = String.format("counter:%s:%s", key, time);
        String s = redisService.get(secondKey);
        if (null == s) {
            return 0;
        } else {
            return Integer.valueOf(s);
        }
    }
}
