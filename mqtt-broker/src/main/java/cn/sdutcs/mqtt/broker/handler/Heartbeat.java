package cn.sdutcs.mqtt.broker.handler;


import java.io.Serializable;

public class Heartbeat implements Serializable {

    private static final long serialVersionUID = -7175677772224683310L;

    public static final byte[] BYTES = new byte[0];

    private static Heartbeat instance = new Heartbeat();

    public static Heartbeat getSingleton() {
        return instance;
    }

    private Heartbeat() {
    }
}
