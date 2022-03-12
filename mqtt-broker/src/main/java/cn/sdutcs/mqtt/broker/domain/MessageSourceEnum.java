package cn.sdutcs.mqtt.broker.domain;

public enum MessageSourceEnum {

    DEVICE("DEVICE"),

    PLATFORM("PLATFORM");

    private String code;

    MessageSourceEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}