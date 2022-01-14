package cn.sdutcs.mqtt.auth.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Scanner;

/**
 * 密码
 */
public class PwdUtil {

    /**
     * 通过用户名和私钥生成密码
     */
    public static void main(String[] args) throws IOException {
        System.out.print("输入需要获取密码的用户名: ");
        Scanner scanner = new Scanner(System.in);
        String value = scanner.nextLine();
        RSAPrivateKey privateKey = IoUtil.readObj(PwdUtil.class.getClassLoader().getResourceAsStream("keystore/auth-private.key"));
        RSA rsa = new RSA(privateKey, null);
        System.out.println("用户名: " + value + " 对应生成的密码为: " + rsa.encryptBcd(value, KeyType.PrivateKey));
        // 用户名: devhg
        // 对应生成的密码为: AFF4FEA3B51EC536631017BC736A8CC55BCF9EB13B179AE4CB35DB212300011C99EAF63368342DAD801245C7BAE78DF6B8BA99E98B74B645EB4CF7F67A838F98
    }

}
