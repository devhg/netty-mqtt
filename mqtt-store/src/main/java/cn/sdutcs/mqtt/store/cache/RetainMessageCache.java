package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.message.RetainMessageStore;
import cn.sdutcs.mqtt.store.utils.Util;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RetainMessageCache {
    private final static String CACHE_PRE = "mqtt:retain:";
    private Jedis redisService;
    // private JedisAgent jedisAgent;

    public RetainMessageStore put(String topic, RetainMessageStore obj) {
        redisService.set(CACHE_PRE + topic, JSONObject.toJSONString(obj));
        return obj;
    }

    public RetainMessageStore get(String topic) {
        return JSONObject.parseObject(redisService.get(CACHE_PRE + topic), RetainMessageStore.class);
    }

    public boolean containsKey(String topic) {
        return redisService.exists(CACHE_PRE + topic);
    }

    public void remove(String topic) {
        redisService.del(CACHE_PRE + topic);
    }

    public Map<String, RetainMessageStore> all() {
        Map<String, RetainMessageStore> map = new HashMap<>();
        ScanParams match = new ScanParams().match(CACHE_PRE + "*");
        List<String> keys = new ArrayList<>();
        if (false /*jedisAgent.isClusterMode()*/) {
            // JedisCluster jedisCluster = jedisAgent.getJedisClusterWrapper().getJedisCluster();
            // for (JedisPool pool : jedisCluster.getClusterNodes().values()) {
            //     try (Jedis jedis = pool.getResource()) {
            //         ScanResult<String> scan = null;
            //         do {
            //             scan = jedis.scan(scan == null ? ScanParams.SCAN_POINTER_START : scan.getStringCursor(), match);
            //             keys.addAll(scan.getResult());
            //         } while (!scan.isCompleteIteration());
            //     }
            // }
        } else {
            // Jedis jedis = null;
            try {
                // jedis = jedisAgent.jedis();
                ScanResult<String> scan = null;
                do {
                    scan = redisService.scan(scan == null ? ScanParams.SCAN_POINTER_START : scan.getCursor(), match);
                    keys.addAll(scan.getResult());
                } while (!scan.isCompleteIteration());
            } finally {
                // Util.safeClose(jedis);
            }
        }
        for (String key : keys) {
            map.put(key.substring(CACHE_PRE.length()), JSONObject.parseObject(redisService.get(key), RetainMessageStore.class));
        }
        return map;
    }
}
