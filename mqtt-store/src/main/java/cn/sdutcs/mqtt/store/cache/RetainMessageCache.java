package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.message.RetainMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RetainMessageCache {
    private final static String CACHE_PRE = "mqtt:retain:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public RetainMessageStore put(String topic, RetainMessageStore obj) {
        redisTemplate.opsForValue().set(CACHE_PRE + topic, JSONObject.toJSONString(obj));
        return obj;
    }

    public RetainMessageStore get(String topic) {
        return JSONObject.parseObject(redisTemplate.opsForValue().get(CACHE_PRE + topic), RetainMessageStore.class);
    }

    public boolean containsKey(String topic) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CACHE_PRE + topic));
    }

    public void remove(String topic) {
        redisTemplate.delete(CACHE_PRE + topic);
    }

    /**
     * 获取全部key(topic)
     */
    public List<String> keys() {
        // ScanParams match = new ScanParams().match(CACHE_PRE + "*");
        Set<String> keys = new HashSet<>();
        try {
            redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                try (Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder()
                        .match(CACHE_PRE + "*")
                        .count(10000).build())) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return keys;
            });

        } finally {
            // Util.safeClose(jedis);
        }
        return keys.stream().map((key) -> {
            return key.substring(CACHE_PRE.length());
        }).collect(Collectors.toList());
    }
}
