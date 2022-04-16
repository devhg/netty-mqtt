package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class QpsCounter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QpsCounter.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ConcurrentHashMap<String, AtomicLong> localCounter;

    /**
     * Qps 计数。本地计数，定时刷入Redis的思路
     */
    public void Count(String key) {
        String now = DateUtil.now("yyyy-MM-dd-HH-mm-ss");
        if (localCounter.containsKey(now)) {
            localCounter.get(now).incrementAndGet();
        } else {
            localCounter.put(now, new AtomicLong(1));
        }
        // 方式1 [deprecated]
        // String secondKey = String.format("counter:%s:%s", key, now);
        // Long count = redisTemplate.opsForValue().increment(secondKey);
        // if (count != null && 1 == count) {
        //     redisTemplate.expire(secondKey, 300, TimeUnit.SECONDS);
        // }
    }

    @Scheduled(fixedRate = 2000)
    public void qpsScheduledTask() {
        String now = DateUtil.now("yyyy-MM-dd-HH-mm-ss");
        localCounter.forEach((k, v) -> {
            // 已经不是当前时间（秒级）
            if (!k.equals(now)) {
                System.out.println(k + "   " + v.get());
                String secondKey = String.format("counter:qps:%s", k);
                // todo redis事务
                redisTemplate.opsForValue().set(secondKey, String.valueOf(v.get()), 300, TimeUnit.SECONDS);
                localCounter.remove(k);
            }
        });
    }

    /**
     * 获取第 time 秒的 Request 数目
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
