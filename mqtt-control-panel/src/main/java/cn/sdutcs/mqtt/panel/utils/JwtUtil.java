package cn.sdutcs.mqtt.panel.utils;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import cn.sdutcs.mqtt.panel.common.Constant;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.jsonwebtoken.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

/**
 * @author devhg
 * @Description: JWT工具类JSON WEB TOKEN
 */
public class JwtUtil {

    /**
     * 签发JWT
     */
    public static String createJWT(String id, String subject, long ttlMillis) {
        byte[] key = Constant.JWT_SECRET.getBytes();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        // System.out.println("subject = " + subject);
        return JWT.create()
                .setPayload("id", id)
                .setPayload("admin", true)
                .setPayload("subs", subject)
                .setIssuer(Constant.ISSUER)
                .setAudience(Constant.AUD)
                .setIssuedAt(now)
                .setExpiresAt(new Date(nowMillis + ttlMillis))
                .setKey(key)
                .sign();
    }

    public static Boolean validateJWT(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        byte[] key = Constant.JWT_SECRET.getBytes();

        JWT jwt = JWT.of(token);
        // System.out.println("jwt.getPayload(\"id\") = " + jwt.getPayload("id"));
        // System.out.println("jwt.getPayload(\"admin\") = " + jwt.getPayload("admin"));
        // System.out.println("jwt.getPayload(\"sub\") = " + jwt.getPayload("subs"));

        try {
            JWTValidator.of(token)
                    .validateAlgorithm(JWTSignerUtil.hs256(key))
                    .validateDate(new Date());
        } catch (Exception e) {
            if (e instanceof ValidateException) {
                return false;
            }
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 构建密钥
     *
     * @return SecretKey
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.decode(Constant.JWT_SECRET);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    /**
     * 解析
     */
    public static Claims parseJWT(String jwt) {
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();

    }
}