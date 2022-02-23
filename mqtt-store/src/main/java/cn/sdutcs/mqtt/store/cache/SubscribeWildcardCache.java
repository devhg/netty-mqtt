package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.*;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模糊topic
 */
@Repository
public class SubscribeWildcardCache {
    private final static String CACHE_PRE = "mqtt:subwildcard:";
    private final static String CACHE_CLIENT_PRE = "mqtt:client:";

    @Autowired
    private UnifiedJedis redisService;
    // private JedisAgent jedisAgent;

    public SubscribeStore put(String topic, String clientId, SubscribeStore subscribeStore) {
        redisService.hset(CACHE_PRE + topic, clientId, JSONObject.toJSONString(subscribeStore));
        redisService.sadd(CACHE_CLIENT_PRE + clientId, topic);
        return subscribeStore;
    }

    public SubscribeStore get(String topic, String clientId) {
        return JSONObject.parseObject(redisService.hget(CACHE_PRE + topic, clientId), SubscribeStore.class);
    }

    public boolean containsKey(String topic, String clientId) {
        return redisService.hexists(CACHE_PRE + topic, clientId);
    }

    public void remove(String topic, String clientId) {
        redisService.srem(CACHE_CLIENT_PRE + clientId, topic);
        redisService.hdel(CACHE_PRE + topic, clientId);
    }

    public void removeForClient(String clientId) {
        for (String topic : redisService.smembers(CACHE_CLIENT_PRE + clientId)) {
            redisService.hdel(CACHE_PRE + topic, clientId);
        }
        redisService.del(CACHE_CLIENT_PRE + clientId);
    }

    public Map<String, ConcurrentHashMap<String, SubscribeStore>> all() {
        Map<String, ConcurrentHashMap<String, SubscribeStore>> result = new HashMap<>();
        ScanParams match = new ScanParams().match(CACHE_PRE + "*");
        List<String> keys = new ArrayList<>();
        try {
            ScanResult<String> scan = null;
            do {
                scan = redisService.scan(scan == null ? ScanParams.SCAN_POINTER_START : scan.getCursor(), match);
                keys.addAll(scan.getResult());
            } while (!scan.isCompleteIteration());
        } finally {
            // Streams.safeClose(jedis);
        }
        for (String key : keys) {
            ConcurrentHashMap<String, SubscribeStore> map1 = new ConcurrentHashMap<>();
            Map<String, String> map2 = redisService.hgetAll(key);
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
        Map<String, String> map = redisService.hgetAll(CACHE_PRE + topic);
        if (map != null && !map.isEmpty()) {
            map.forEach((k, v) -> {
                list.add(JSONObject.parseObject(v, SubscribeStore.class));
            });
        }
        return list;
    }
}
