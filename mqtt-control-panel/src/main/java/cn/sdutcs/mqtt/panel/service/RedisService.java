package cn.sdutcs.mqtt.panel.service;

import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import cn.sdutcs.mqtt.panel.common.Constant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 获取broker配置信息
     */
    public Map getBrokerConfig() {
        String broker_config = redisTemplate.opsForValue().get("broker_config");
        return JSON.parseObject(broker_config, Map.class);
    }

    private Map<String, Long> getTopics(String topicType) {
        Map<String, Long> result = new HashMap<>();
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder()
                    .match(topicType + "*")
                    .count(10000).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return keys;
        });

        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        for (String key : keys) {
            ConcurrentHashMap<String, SubscribeStore> map1 = new ConcurrentHashMap<>();
            Map<String, String> map2 = hash.entries(key);
            result.put(key.substring(topicType.length()), hash.size(key));
        }
        return result;
    }

    public Map<String, Map<String, Long>> getTopicList() {
        Map<String, Long> wild = this.getTopics(Constant.TOPIC_CACHE_PRE_WILD);
        Map<String, Long> notwild = this.getTopics(Constant.TOPIC_CACHE_PRE_NOT_WILD);
        Map<String, Map<String, Long>> result = new HashMap<>();
        result.put("wild", wild);
        result.put("notwild", notwild);
        return result;
    }

    public Set<String> getTopicListByClientId(String clientId) {
        SetOperations<String, String> set = redisTemplate.opsForSet();
        return set.members(Constant.CACHE_CLIENT_PRE + clientId);
    }

    public List<SubscribeStore> getTopicSubscriber(String topicType, String topic) {
        List<SubscribeStore> list = new ArrayList<>();
        HashOperations<String, String, String> hash = redisTemplate.opsForHash();
        Map<String, String> map = hash.entries(topicType + topic);
        if (map != null && !map.isEmpty()) {
            map.forEach((k, v) -> {
                list.add(JSONObject.parseObject(v, SubscribeStore.class));
            });
        }
        return list;
    }
}
