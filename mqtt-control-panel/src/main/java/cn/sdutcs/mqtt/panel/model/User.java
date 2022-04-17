package cn.sdutcs.mqtt.panel.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements Serializable {

    private static final long serialVersionUID = -7873426366054378670L;

    private Long id;
    private String token;
    private String username;
    private String password;
    private String email;
    private String nickName;

    public User(String token, String username, String email, String nickName) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}

