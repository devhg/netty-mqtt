package cn.sdutcs.mqtt.common.auth;

/**
 * 用户和密码认证服务接口
 */
public interface IAuthService {

    /**
     * 验证用户名和密码是否正确
     */
    boolean checkValid(String username, String password);

    String genPassword(String username);

}
