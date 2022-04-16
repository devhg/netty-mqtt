package cn.sdutcs.mqtt.panel.service;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.common.session.SessionStore;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ISessionStoreService {

    private final static String CACHE_PRE = "mqtt:session:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public List<Map> getSessionList() {
        Set<String> clients = redisTemplate.opsForSet().members("clients");
        return Objects.requireNonNull(clients).stream().map((clientId) -> {
            String jsonObj = redisTemplate.opsForValue().get(CACHE_PRE + clientId);
            if (StrUtil.isNotBlank(jsonObj)) {
                return JSON.parseObject(jsonObj, Map.class);
            } else {
                return new SessionStore("null", clientId, "unActive").toMap();
            }
        }).collect(Collectors.toList());
    }

    public Boolean remove(String clientId) {
        return redisTemplate.delete(CACHE_PRE + clientId);
    }
}
