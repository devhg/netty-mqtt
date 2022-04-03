package cn.sdutcs.mqtt.panel.model;

import lombok.Data;

import java.io.Serializable;


@Data
public class User implements Serializable {

    private static final long serialVersionUID = -7873426366054378670L;

    private Long id;
    private String userName;
    private String passWord;
    private Integer userSex;
    private String nickName;

    public User() {
        super();
    }

    public User(String userName, String passWord) {
        super();
        this.passWord = passWord;
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}

