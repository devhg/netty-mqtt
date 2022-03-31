package cn.sdutcs.mqtt.store.session;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.common.session.ISessionStoreService;
import cn.sdutcs.mqtt.common.session.SessionStore;
import cn.sdutcs.mqtt.store.starter.StoreStarter;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会话存储服务
 */
@Service
public class SessionStoreService implements ISessionStoreService {

    private final static String CACHE_PRE = "mqtt:session:";

    @Autowired
    private StoreStarter storeStarter;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void put(String clientId, SessionStore sessionStore, int expire) {
        redisTemplate.opsForSet().add("clients", clientId);
        if (expire > 0) {
            redisTemplate.opsForValue().setIfAbsent(CACHE_PRE + clientId,
                    JSON.toJSONString(sessionStore.toMap()), expire, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(CACHE_PRE + clientId, JSON.toJSONString(sessionStore.toMap()));
        }
    }

    @Override
    public void expire(String clientId, int expire) {
        redisTemplate.expire(CACHE_PRE + clientId, expire, TimeUnit.SECONDS);
    }

    @Override
    public SessionStore get(String clientId) {
        String jsonObj = redisTemplate.opsForValue().get(CACHE_PRE + clientId);
        if (StrUtil.isNotBlank(jsonObj)) {
            Map map = JSON.parseObject(jsonObj, Map.class);
            return SessionStore.ValueOf(map);
        }
        return null;
    }

    @Override
    public boolean containsKey(String clientId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CACHE_PRE + clientId));
    }

    @Override
    public void remove(String clientId) {
        redisTemplate.delete(CACHE_PRE + clientId);
    }

    @Override
    public List<Map> getSessionList() {
        Set<String> clients = redisTemplate.opsForSet().members("clients");
        return clients.stream().map((clientId) -> {
            String jsonObj = redisTemplate.opsForValue().get(CACHE_PRE + clientId);
            if (StrUtil.isNotBlank(jsonObj)) {
                return JSON.parseObject(jsonObj, Map.class);
            } else {
                return new SessionStore(storeStarter.PROP_INSTANCENAME, clientId, "unActive").toMap();
            }
        }).collect(Collectors.toList());
    }
}
