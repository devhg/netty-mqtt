package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.message.DupPublishMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DupPublishMessageCache {
    private final static String CACHE_PRE = "mqtt:publish:";

    @Autowired
    private JedisPooled redisService;

    public DupPublishMessageStore put(String clientId, Integer messageId, DupPublishMessageStore dupPublishMessageStore) {
        redisService.hset(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPublishMessageStore));
        return dupPublishMessageStore;
    }

    public ConcurrentHashMap<Integer, DupPublishMessageStore> get(String clientId) {
        ConcurrentHashMap<Integer, DupPublishMessageStore> result = new ConcurrentHashMap<>();
        Map<String, String> map1 = redisService.hgetAll(CACHE_PRE + clientId);
        if (map1 != null && !map1.isEmpty()) {
            map1.forEach((k, v) -> {
                result.put(Integer.valueOf(k), JSONObject.parseObject(v, DupPublishMessageStore.class));
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
