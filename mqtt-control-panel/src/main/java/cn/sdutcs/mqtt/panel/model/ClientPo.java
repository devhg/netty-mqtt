package cn.sdutcs.mqtt.panel.model;

import lombok.Data;

import java.util.Date;

@Data
public class ClientPo {
    private Long id;
    private String email;
    private String phone;
    private String clientId;
    private String clientName;
    private String groupName;
    private String opUser;
    private Date createTime;
}
