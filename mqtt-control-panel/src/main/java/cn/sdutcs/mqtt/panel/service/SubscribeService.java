package cn.sdutcs.mqtt.panel.service;

import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import cn.sdutcs.mqtt.panel.common.Constant;
import cn.sdutcs.mqtt.panel.model.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SubscribeService {
    @Autowired
    private RedisService redisService;

    public List<Topic> getTopicList() {
        List<Topic> result = new ArrayList<>();
        Map<String, Map<String, Long>> ret = redisService.getTopicList();
        Map<String, Long> wild = ret.get("wild");
        AtomicInteger id = new AtomicInteger(1);
        wild.forEach((topic, count) -> {
            List<SubscribeStore> topicSubscriber = redisService.getTopicSubscriber(Constant.TOPIC_CACHE_PRE_WILD, topic);
            result.add(new Topic(id.getAndIncrement(), "wild", topic, count, new Date(), topicSubscriber));
        });
        Map<String, Long> notwild = ret.get("notwild");
        notwild.forEach((topic, count) -> {
            List<SubscribeStore> topicSubscriber = redisService.getTopicSubscriber(Constant.TOPIC_CACHE_PRE_NOT_WILD, topic);
            result.add(new Topic(id.getAndIncrement(), "notwild", topic, count, new Date(), topicSubscriber));
        });
        return result;
    }
}
