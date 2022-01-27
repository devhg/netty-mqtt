package cn.sdutcs.mqtt.store.cache;

import cn.sdutcs.mqtt.common.message.RetainMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.*;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RetainMessageCache {
    private final static String CACHE_PRE = "mqtt:retain:";

    @Autowired
    private UnifiedJedis redisService;

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

    /**
     * 获取全部key(topic)
     */
    public List<String> keys() {
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
            try {
                ScanResult<String> scan = null;
                do {
                    scan = redisService.scan(scan == null ? ScanParams.SCAN_POINTER_START : scan.getCursor(), match);
                    keys.addAll(scan.getResult());
                } while (!scan.isCompleteIteration());
            } finally {
                // Util.safeClose(jedis);
            }
        }
        return keys.stream().map((key) -> {
            return key.substring(CACHE_PRE.length());
        }).collect(Collectors.toList());
    }
}
