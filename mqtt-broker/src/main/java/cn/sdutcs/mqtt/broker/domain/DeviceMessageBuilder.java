package cn.sdutcs.mqtt.broker.domain;

import io.netty.handler.codec.mqtt.MqttPublishMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DeviceMessageBuilder {

    public static final String QOS = "qos";

    public static final String RETAIN = "retain";

    public static final String DUP = "dup";

    public static final String CLEAN_SESSION = "cleansession";

    public static DeviceMessage buildDeviceMessage(boolean retain, boolean dup, int qos, String topic, byte[] content) {
        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setId(null);
        deviceMessage.setContent(content);
        deviceMessage.setSource(MessageSourceEnum.DEVICE);
        deviceMessage.setStoredTime(new Date());
        deviceMessage.setTopic(topic);

        Map<String, Object> properties = new HashMap<>();
        properties.put(QOS, qos);
        properties.put(RETAIN, retain);
        properties.put(DUP, dup);
        deviceMessage.setProperties(properties);
        return deviceMessage;
    }

    public static DeviceMessage buildDeviceMessage(MqttPublishMessage mqttPublishMessage) {
        return buildDeviceMessage(mqttPublishMessage.fixedHeader().isRetain(),
                mqttPublishMessage.fixedHeader().isDup(),
                mqttPublishMessage.fixedHeader().qosLevel().value(),
                mqttPublishMessage.variableHeader().topicName(),
                MqttMessageHelper.readBytesFromByteBuf(mqttPublishMessage.content()));
    }
}
