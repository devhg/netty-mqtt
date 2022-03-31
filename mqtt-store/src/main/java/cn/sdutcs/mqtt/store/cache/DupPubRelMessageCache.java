package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.message.DupPubRelMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DupPubRelMessageCache {
    private final static String CACHE_PRE = "mqtt:pubrel:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public DupPubRelMessageStore put(String clientId, Integer messageId, DupPubRelMessageStore dupPubRelMessageStore) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPubRelMessageStore));
        return dupPubRelMessageStore;
    }

    public ConcurrentHashMap<Integer, DupPubRelMessageStore> get(String clientId) {
        ConcurrentHashMap<Integer, DupPubRelMessageStore> result = new ConcurrentHashMap<>();
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        Map<String, String> map1 = hash.entries(CACHE_PRE + clientId);
        if (map1 != null && !map1.isEmpty()) {
            map1.forEach((k, v) -> {
                result.put(Integer.valueOf(k), JSONObject.parseObject(v, DupPubRelMessageStore.class));
            });
        }
        return result;
    }

    public boolean containsKey(String clientId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CACHE_PRE + clientId));
    }

    public void remove(String clientId, Integer messageId) {
        redisTemplate.opsForHash().delete(CACHE_PRE + clientId, String.valueOf(messageId));
    }

    public void remove(String clientId) {
        redisTemplate.delete(CACHE_PRE + clientId);
    }
}
