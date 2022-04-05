package cn.sdutcs.mqtt.panel.config;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClusterInfo implements Serializable {
    private static final long serialVersionUID = 6852713054542134191L;

    private final String Version = "1.0-SNAPSHOT";
    private final String JVM = "1.8";
    private final String MySQL = "8.0.25";
    private final String Redis = "latest";
    private final String Netty = "4.1.42.Final";
    private final String SpringBoot = "2.1.6.RELEASE";
}
