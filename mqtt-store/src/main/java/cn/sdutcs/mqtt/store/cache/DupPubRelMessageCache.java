package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.message.DupPubRelMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DupPubRelMessageCache {
    private final static String CACHE_PRE = "mqtt:pubrel:";
    private Jedis redisService;

    public DupPubRelMessageStore put(String clientId, Integer messageId, DupPubRelMessageStore dupPubRelMessageStore) {
        redisService.hset(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPubRelMessageStore));
        return dupPubRelMessageStore;
    }

    public ConcurrentHashMap<Integer, DupPubRelMessageStore> get(String clientId) {
        ConcurrentHashMap<Integer, DupPubRelMessageStore> result = new ConcurrentHashMap<>();
        Map<String, String> map1 = redisService.hgetAll(CACHE_PRE + clientId);
        if (map1 != null && !map1.isEmpty()) {
            map1.forEach((k, v) -> {
                result.put(Integer.valueOf(k), JSONObject.parseObject(v, DupPubRelMessageStore.class));
            });
        }
        return result;
    }

    public boolean containsKey(String clientId) {
        return redisService.exists(CACHE_PRE + clientId);
    }

    public void remove(String clientId, Integer messageId) {
        redisService.hdel(CACHE_PRE + clientId, String.valueOf(messageId));
    }

    public void remove(String clientId) {
        redisService.del(CACHE_PRE + clientId);
    }
}
