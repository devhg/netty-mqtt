package cn.sdutcs.mqtt.panel.service;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.panel.dao.UserMapper;
import cn.sdutcs.mqtt.panel.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User getUserByUnameAndPwd(String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return null;
        }
        return userMapper.validLogin(username, password);
    }
}
