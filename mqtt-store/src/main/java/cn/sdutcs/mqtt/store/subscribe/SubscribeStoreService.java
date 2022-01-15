package cn.sdutcs.mqtt.store.subscribe;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.common.subscribe.ISubscribeStoreService;
import cn.sdutcs.mqtt.common.subscribe.SubscribeStore;
import cn.sdutcs.mqtt.store.cache.SubscribeNotWildcardCache;
import cn.sdutcs.mqtt.store.cache.SubscribeWildcardCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 订阅存储服务
 */
@Service
public class SubscribeStoreService implements ISubscribeStoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeStoreService.class);

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
        LOGGER.info("{} subscribe {} success", subscribeStore.getClientId(), topicFilter);
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            subscribeWildcardCache.remove(topicFilter, clientId);
        } else {
            subscribeNotWildcardCache.remove(topicFilter, clientId);
        }
        LOGGER.info("{} unsubscribe {} success", clientId, topicFilter);
    }

    @Override
    public void removeForClient(String clientId) {
        subscribeNotWildcardCache.removeForClient(clientId);
        subscribeWildcardCache.removeForClient(clientId);
        LOGGER.info("{} unsubscribe all topics success", clientId);
    }

    @Override
    public List<SubscribeStore> search(String topic) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        List<SubscribeStore> list = subscribeNotWildcardCache.all(topic);
        if (list.size() > 0) {
            subscribeStores.addAll(list);
        }
        subscribeWildcardCache.all().forEach((topicFilter, map) -> {
            List<String> topicEle = StrUtil.split(topic, '/');
            List<String> filterEle = StrUtil.split(topicFilter, '/');
            if (topicEle.size() >= filterEle.size()) {
                // 对topic重置替换，如果和topicFilter相同，则证明match
                String expected = "";
                for (int i = 0; i < filterEle.size(); i++) {
                    String value = filterEle.get(i);
                    if (value.equals("+")) {
                        expected = expected + "+/";
                    } else if (value.equals("#")) {
                        expected = expected + "#/";
                        break;
                    } else {
                        expected = expected + topicEle.get(i) + "/";
                    }
                }
                expected = StrUtil.removeSuffix(expected, "/");
                if (topicFilter.equals(expected)) {
                    // List<SubscribeStore> list2 = new ArrayList<SubscribeStore>(map.values());
                    subscribeStores.addAll(map.values());
                }
            }
        });
        LOGGER.info("topic={} SubscribeStores {}", topic, subscribeStores);
        return subscribeStores;
    }
}
