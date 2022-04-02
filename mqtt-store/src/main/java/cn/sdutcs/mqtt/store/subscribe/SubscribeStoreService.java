package cn.sdutcs.mqtt.store.subscribe;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import cn.sdutcs.mqtt.store.cache.SubscribeNotWildcardCache;
import cn.sdutcs.mqtt.store.cache.SubscribeWildcardCache;
import cn.sdutcs.mqtt.store.starter.StoreStarter;
import cn.sdutcs.mqtt.store.utils.TopicMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 订阅存储服务
 */
@Service
public class SubscribeStoreService implements ISubscribeStoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeStoreService.class);

    @Autowired
    private StoreStarter storeStarter;
    @Autowired
    private SubscribeNotWildcardCache subscribeNotWildcardCache;
    @Autowired
    private SubscribeWildcardCache subscribeWildcardCache;

    @Override
    public void put(String topicFilter, SubscribeStore subscribeStore) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            subscribeWildcardCache.put(topicFilter, subscribeStore.getClientId(), subscribeStore);
        } else {
            subscribeNotWildcardCache.put(topicFilter, subscribeStore.getClientId(), subscribeStore);
        }
        LOGGER.debug("{} subscribe {} success", subscribeStore.getClientId(), topicFilter);
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            subscribeWildcardCache.remove(topicFilter, clientId);
        } else {
            subscribeNotWildcardCache.remove(topicFilter, clientId);
        }
        LOGGER.debug("{} unsubscribe {} success", clientId, topicFilter);
    }

    @Override
    public void removeForClient(String clientId) {
        subscribeNotWildcardCache.removeForClient(clientId);
        subscribeWildcardCache.removeForClient(clientId);
        LOGGER.debug("{} unsubscribe all topics success", clientId);
    }

    @Override
    public List<SubscribeStore> search(String topic) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        List<SubscribeStore> notWildSubscribeStores = subscribeNotWildcardCache.all(topic);
        if (notWildSubscribeStores.size() > 0) {
            subscribeStores.addAll(notWildSubscribeStores);
        }
        subscribeWildcardCache.all().forEach((topicFilter, map) -> {
            List<String> topicEle = StrUtil.split(topic, '/');
            List<String> filterEle = StrUtil.split(topicFilter, '/');
            if (TopicMatcher.match(topicFilter, topic)) {
                subscribeStores.addAll(map.values());
            }

        });
        LOGGER.debug("broker={} forward message to topic={}, count SubscribeStores={}", storeStarter.getPROP_INSTANCE_NAME(),
                topic, subscribeStores.size());
        return subscribeStores;
    }
}
