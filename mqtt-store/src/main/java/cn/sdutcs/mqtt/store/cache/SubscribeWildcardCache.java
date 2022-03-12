package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.*;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模糊topic
 */
@Repository
public class SubscribeWildcardCache {
    private final static String CACHE_PRE = "mqtt:subwildcard:";
    private final static String CACHE_CLIENT_PRE = "mqtt:client:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    public SubscribeStore put(String topic, String clientId, SubscribeStore subscribeStore) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        SetOperations<String, String> set = redisTemplate.opsForSet();
        hash.put(CACHE_PRE + topic, clientId, JSONObject.toJSONString(subscribeStore));
        set.add(CACHE_CLIENT_PRE + clientId, topic);
        return subscribeStore;
    }

    public SubscribeStore get(String topic, String clientId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return JSONObject.parseObject((String) hash.get(CACHE_PRE + topic, clientId), SubscribeStore.class);
    }

    public boolean containsKey(String topic, String clientId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.hasKey(CACHE_PRE + topic, clientId);
    }

    public void remove(String topic, String clientId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        SetOperations<String, String> set = redisTemplate.opsForSet();
        set.remove(CACHE_CLIENT_PRE + clientId, topic);
        hash.delete(CACHE_PRE + topic, clientId);
    }

    public void removeForClient(String clientId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        SetOperations<String, String> set = redisTemplate.opsForSet();
        for (String topic : Objects.requireNonNull(set.members(CACHE_CLIENT_PRE + clientId))) {
            hash.delete(CACHE_PRE + topic, clientId);
        }
        redisTemplate.delete(CACHE_CLIENT_PRE + clientId);
    }

    public Map<String, ConcurrentHashMap<String, SubscribeStore>> all() {
        Map<String, ConcurrentHashMap<String, SubscribeStore>> result = new HashMap<>();
        ScanParams match = new ScanParams().match(CACHE_PRE + "*");
        Set<String> keys = new HashSet<>();
        try {
            // ScanResult<String> scan = null;
            // do {
            //     scan = redisService.scan(scan == null ? ScanParams.SCAN_POINTER_START : scan.getCursor(), match);
            //     keys.addAll(scan.getResult());
            // } while (!scan.isCompleteIteration());

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
            // Streams.safeClose(jedis);
        }
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        for (String key : keys) {
            ConcurrentHashMap<String, SubscribeStore> map1 = new ConcurrentHashMap<>();
            Map<String, String> map2 = hash.entries(key);
            if (map2 != null && !map2.isEmpty()) {
                map2.forEach((k, v) -> {
                    map1.put(k, JSONObject.parseObject(v, SubscribeStore.class));
                });
                result.put(key.substring(CACHE_PRE.length()), map1);
            }
        }
        return result;
    }

    public List<SubscribeStore> all(String topic) {
        List<SubscribeStore> list = new ArrayList<>();
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        Map<String, String> map = hash.entries(CACHE_PRE + topic);
        if (map != null && !map.isEmpty()) {
            map.forEach((k, v) -> {
                list.add(JSONObject.parseObject(v, SubscribeStore.class));
            });
        }
        return list;
    }
}
