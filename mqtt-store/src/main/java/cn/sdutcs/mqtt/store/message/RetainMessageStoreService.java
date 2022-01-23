package cn.sdutcs.mqtt.store.message;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.common.message.IRetainMessageStoreService;
import cn.sdutcs.mqtt.common.message.RetainMessageStore;
import cn.sdutcs.mqtt.store.cache.RetainMessageCache;
import cn.sdutcs.mqtt.store.utils.TopicMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RetainMessageStoreService implements IRetainMessageStoreService {

    @Autowired
    private RetainMessageCache retainMessageCache;

    @Override
    public void put(String topic, RetainMessageStore retainMessageStore) {
        retainMessageCache.put(topic, retainMessageStore);
    }

    @Override
    public RetainMessageStore get(String topic) {
        return retainMessageCache.get(topic);
    }

    @Override
    public void remove(String topic) {
        retainMessageCache.remove(topic);
    }

    @Override
    public boolean containsKey(String topic) {
        return retainMessageCache.containsKey(topic);
    }

    @Override
    public List<RetainMessageStore> search(String topicFilter) {
        List<RetainMessageStore> retainMessageStores = new ArrayList<RetainMessageStore>();
        if (!StrUtil.contains(topicFilter, '#') && !StrUtil.contains(topicFilter, '+')) {
            // 非模糊匹配
            if (retainMessageCache.containsKey(topicFilter)) {
                retainMessageStores.add(retainMessageCache.get(topicFilter));
            }
        } else {
            // 模糊匹配
            retainMessageCache.keys().forEach((topic) -> {
                List<String> splitTopic = StrUtil.split(topic, '/');
                List<String> splitTopicFilters = StrUtil.split(topicFilter, '/');
                if (TopicMatcher.match(topicFilter, topic)) {
                    retainMessageStores.add(retainMessageCache.get(topic));
                }
            });
        }
        return retainMessageStores;
    }
}
