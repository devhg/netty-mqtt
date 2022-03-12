package cn.sdutcs.mqtt.common.message;

/**
 * 分布式生成报文标识符
 */
public interface IMessageIdService {

    /**
     * 获取报文标识符
     * @return
     */
    int getNextMessageId();
}
