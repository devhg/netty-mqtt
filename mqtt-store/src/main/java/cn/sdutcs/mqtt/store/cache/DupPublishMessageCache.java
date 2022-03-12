package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.message.DupPublishMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.UnifiedJedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DupPublishMessageCache {
    private final static String CACHE_PRE = "mqtt:publish:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public DupPublishMessageStore put(String clientId, Integer messageId,
                                      DupPublishMessageStore dupPublishMessageStore) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPublishMessageStore));
        return dupPublishMessageStore;
    }

    public ConcurrentHashMap<Integer, DupPublishMessageStore> get(String clientId) {
        ConcurrentHashMap<Integer, DupPublishMessageStore> result = new ConcurrentHashMap<>();
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        Map<String, String> map1 = hash.entries(CACHE_PRE + clientId);
        if (map1 != null && !map1.isEmpty()) {
            map1.forEach((k, v) -> {
                result.put(Integer.valueOf(k), JSONObject.parseObject(v, DupPublishMessageStore.class));
            });
        }
        return result;
    }

    public boolean containsKey(String clientId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CACHE_PRE + clientId));
    }

    public void remove(String clientId, Integer messageId) {
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        hash.delete(CACHE_PRE + clientId, String.valueOf(messageId));
    }

    public void remove(String clientId) {
        redisTemplate.delete(CACHE_PRE + clientId);
    }
}
