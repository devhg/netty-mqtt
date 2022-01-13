package cn.sdutcs.mqtt.store.message;

import cn.sdutcs.mqtt.common.message.DupPubRelMessageStore;
import cn.sdutcs.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.sdutcs.mqtt.common.message.IMessageIdService;
import cn.sdutcs.mqtt.store.cache.DupPubRelMessageCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DupPubRelMessageStoreService implements IDupPubRelMessageStoreService {

    @Autowired
    private IMessageIdService messageIdService;
    @Autowired
    private DupPubRelMessageCache dupPubRelMessageCache;

    @Override
    public void put(String clientId, DupPubRelMessageStore dupPubRelMessageStore) {
        dupPubRelMessageCache.put(clientId, dupPubRelMessageStore.getMessageId(), dupPubRelMessageStore);
    }

    @Override
    public List<DupPubRelMessageStore> get(String clientId) {
        if (dupPubRelMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
            Collection<DupPubRelMessageStore> collection = map.values();
            return new ArrayList<>(collection);
        }
        return new ArrayList<>();
    }

    @Override
    public void remove(String clientId, int messageId) {
        dupPubRelMessageCache.remove(clientId, messageId);
    }

    @Override
    public void removeByClient(String clientId) {
        if (dupPubRelMessageCache.containsKey(clientId)) {
            dupPubRelMessageCache.remove(clientId);
        }
    }
}
