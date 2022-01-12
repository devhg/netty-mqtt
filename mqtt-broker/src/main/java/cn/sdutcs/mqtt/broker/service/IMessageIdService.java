package cn.sdutcs.mqtt.broker.service;

/**
 * 分布式生成报文标识符
 */
public interface IMessageIdService {

    /**
     * 获取报文标识符
     */
    int getNextMessageId();
}
