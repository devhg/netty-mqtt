package cn.sdutcs.mqtt.panel.service;

import cn.sdutcs.mqtt.panel.dao.BlackListMapper;
import cn.sdutcs.mqtt.panel.model.BlackIP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlackListService {
    @Autowired
    BlackListMapper blackListMapper;

    public boolean onBlackList(String hostAddress) {
        // System.out.println("hostAddress = " + hostAddress); // 127.0.0.1:52272
        // todo 支持 127.0.0.1:*
        String[] hostPort = hostAddress.split(":");
        // System.out.println(hostPort[0]);
        BlackIP blackIP = blackListMapper.getOne(hostPort[0]);
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
