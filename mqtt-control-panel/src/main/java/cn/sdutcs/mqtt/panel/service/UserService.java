package cn.sdutcs.mqtt.panel.service;

import cn.sdutcs.mqtt.panel.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
}
