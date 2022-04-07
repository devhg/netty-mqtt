package cn.sdutcs.mqtt.auth.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.sdutcs.mqtt.auth.util.PwdUtil;
import cn.sdutcs.mqtt.common.auth.IAuthService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;
import java.util.Scanner;

/**
 * 用户名和密码认证服务
 */
@Service
public class AuthService implements IAuthService {

    private RSAPrivateKey privateKey;

    @Override
    public boolean checkValid(String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return false;
        }
        RSA rsa = new RSA(privateKey, null);
        String value = rsa.encryptBcd(username, KeyType.PrivateKey);
        return value.equals(password);
    }

    @Override
    public String genPassword(String username) {
        RSA rsa = new RSA(privateKey, null);
        // System.out.println("用户名: " + username + " 对应生成的密码为: " + rsa.encryptBcd(username, KeyType.PrivateKey));
        // 用户名: devhg
        // 对应生成的密码为: AFF4FEA3B51EC536631017BC736A8CC55BCF9EB13B179AE4CB35DB212300011C99EAF63368342DAD801245C7BAE78DF6B8BA99E98B74B645EB4CF7F67A838F98
        return rsa.encryptBcd(username, KeyType.PrivateKey);
    }

    @PostConstruct
    public void init() {
        privateKey = IoUtil.readObj(AuthService.class.getClassLoader().getResourceAsStream("keystore/auth-private.key"));
    }
}
