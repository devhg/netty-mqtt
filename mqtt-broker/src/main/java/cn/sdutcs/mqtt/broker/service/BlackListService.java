package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.web.dao.BlackListMapper;
import cn.sdutcs.mqtt.broker.web.model.BlackIP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlackListService {
    @Autowired
    BlackListMapper blackListMapper;

    public boolean checkBlackList(String hostAddress) {
        int isReject = blackListMapper.exist(hostAddress);
        return isReject != 1;
    }

    public boolean addIPToBlackList(BlackIP ip) {
        int ok = blackListMapper.insert(ip);
        return ok == 1;
    }

    public boolean deleteIPFromBlackList(Long ipID) {
        int ok = blackListMapper.delete(ipID);
        return ok == 1;
    }

    public List<BlackIP> fetchIPBlackList() {
        return blackListMapper.getAll();
    }
}
