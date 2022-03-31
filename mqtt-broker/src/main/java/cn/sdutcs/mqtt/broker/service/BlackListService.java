package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.domain.BlackIP;
import cn.sdutcs.mqtt.broker.dao.BlackListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
