package cn.sdutcs.mqtt.broker.handler;

import java.io.Serializable;

public class BaseMessage implements Serializable {
    private static final long serialVersionUID = 3539351082739927064L;

    protected int sequence;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}