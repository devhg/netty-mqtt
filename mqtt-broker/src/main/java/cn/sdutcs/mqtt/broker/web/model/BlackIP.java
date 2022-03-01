package cn.sdutcs.mqtt.broker.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BlackIP implements Serializable {
    private static final long serialVersionUID = 8984467065918931676L;

    private Long id;
    private String ip;
    private int status;
    @JsonProperty("op_user")
    private String opUser;
    private Date createTime;
}
