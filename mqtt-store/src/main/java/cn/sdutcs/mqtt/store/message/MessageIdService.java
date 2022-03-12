package cn.sdutcs.mqtt.store.message;

import cn.sdutcs.mqtt.common.message.IMessageIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MessageIdService implements IMessageIdService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public int getNextMessageId() {
        try {
            while (true) {
                int nextMsgId = Math.toIntExact(redisTemplate.opsForValue().increment("mqtt:messageid:num") % 65536);
                if (nextMsgId > 0) {
                    return nextMsgId;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 每次重启的时候重新初始化
     */
    @PostConstruct
    public void init() {
        if (Boolean.TRUE.equals(redisTemplate.hasKey("mqtt:messageid:num"))) {
            redisTemplate.delete("mqtt:messageid:num");
        }
    }
}
