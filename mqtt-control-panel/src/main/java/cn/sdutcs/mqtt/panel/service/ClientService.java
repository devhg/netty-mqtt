package cn.sdutcs.mqtt.panel.service;

import cn.hutool.core.codec.Base62;
import cn.sdutcs.mqtt.panel.dao.ClientMapper;
import cn.sdutcs.mqtt.panel.model.ClientPo;
import cn.sdutcs.mqtt.panel.model.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientService {
    @Autowired
    private ClientMapper clientMapper;

    @Transactional
    public boolean addNewClient(ClientPo clientPo) throws Exception {
        int affectedRows = clientMapper.insert(clientPo);
        if (affectedRows == 1) {
            ClientPo client = clientMapper.getClientByName(clientPo.getClientName());
            if (client != null) {
                String encode = Base62.encode(Long.toString(client.getId()));
                int updateClientName = clientMapper.updateClientIdByName(client.getClientName(), "client_" + encode);
                return updateClientName == 1;
            }
        }
        return false;
    }

    public boolean deleteFromClientList(Long id) {
        int affectedRows = clientMapper.deleteClientById(id);
        return affectedRows == 1;
    }

    public Map<String, Object> fetchClientList(int page, int pageSize, String groupName, String opUser) {
        int packetsTotal = clientMapper.countClientTotal(groupName, opUser);
        List<ClientPo> clients = clientMapper.fetchAllClients(pageSize, (page - 1) * pageSize, groupName, opUser);
        Map<String, Object> res = new HashMap<>();
        res.put("data", clients);
        res.put("total", packetsTotal);
        return res;
    }
}
