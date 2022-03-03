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
        BlackIP blackIP = blackListMapper.getOne(hostAddress);
        if (null == blackIP) {
            return false;
        }
        return blackIP.getStatus() == 1;
    }

    public boolean addIPToBlackList(BlackIP ip) {
        int affectedRows = blackListMapper.insert(ip);
        return affectedRows == 1;
    }

    public boolean updateIPToBlackList(BlackIP ip) {
        int affectedRows = blackListMapper.update(ip);
        return affectedRows == 1;
    }

    public boolean deleteIPFromBlackList(Long ipID) {
        int affectedRows = blackListMapper.delete(ipID);
        return affectedRows == 1;
    }

    public List<BlackIP> fetchIPBlackList(String ip, String opUser) {
        return blackListMapper.getAll(ip, opUser);
    }
}
