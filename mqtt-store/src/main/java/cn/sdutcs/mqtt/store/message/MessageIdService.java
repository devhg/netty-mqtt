package cn.sdutcs.mqtt.store.message;

import cn.sdutcs.mqtt.common.message.IMessageIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class MessageIdService implements IMessageIdService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdService.class);

    // todo inject
    private Jedis redisService;

    @Override
    public int getNextMessageId() {
        try {
            while (true) {
                int nextMsgId = (int) (redisService.incr("mqtt:messageid:num") % 65536);
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
    public void init() {
        redisService.del("mqtt:messageid:num");
    }
}
