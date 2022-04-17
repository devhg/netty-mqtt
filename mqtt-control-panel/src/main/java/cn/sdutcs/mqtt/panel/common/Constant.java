package cn.sdutcs.mqtt.panel.common;

public class Constant {

    public final static String TOPIC_CACHE_PRE_NOT_WILD = "mqtt:subnotwildcard:";

    public final static String TOPIC_CACHE_PRE_WILD = "mqtt:subwildcard:";

    public final static String CACHE_CLIENT_PRE = "mqtt:client:";

    // 原始码 sdut_jsjxy%softwarelab_9#305_design%by_qxqzx&20191101  md5加密
    public static final String JWT_SECRET = "721d7a1fd082fb4518a76f96327651ff";
    // 签发者
    public static final String ISSUER = "superAdmin";
    // 接收者
    public static final String AUD = "user";
    /****************************************************************/
    // JWT过期错误
    public static final Integer JWT_ERRCODE_EXPIRE = 85201;
    // JWT验证失败
    public static final Integer JWT_ERRCODE_FAIL = 85202;
    /****************************************************************/
    // 请求头
    public static final String HEADER_TOKEN = "Access-Token";
}
