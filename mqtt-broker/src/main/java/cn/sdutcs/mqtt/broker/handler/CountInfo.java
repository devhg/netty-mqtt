package cn.sdutcs.mqtt.broker.handler;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class CountInfo implements Serializable {

    private static final long serialVersionUID = -1705088025632217227L;

    /**
     * 最后次接收消息
     */
    private long lastReceiveTime;
    /**
     * 最后次发送消息
     */
    private long lastSentTime;
    /**
     * 最大连接数
     */
    private long maxChannelNum;
    /**
     * 当前连接数
     */
    private long curChannelNum;
    /**
     * 接收消息数
     */
    private AtomicLong receiveNum = new AtomicLong();
    /**
     * 发送消息数
     */
    private AtomicLong sentNum = new AtomicLong();
    /**
     * 收发心跳数
     */
    private AtomicLong heartbeatNum = new AtomicLong();
    /**
     * CPU使用频率
     */
    private double Cpu;

    public long getCurChannelNum() {
        return curChannelNum;
    }

    public void setCurChannelNum(long curChannelNum) {
        this.curChannelNum = curChannelNum;
        if (this.maxChannelNum < curChannelNum) {
            this.maxChannelNum = curChannelNum;
        }
    }

    public long getMaxChannelNum() {
        return maxChannelNum;
    }

    public AtomicLong getReceiveNum() {
        return receiveNum;
    }

    public AtomicLong getSentNum() {
        return sentNum;
    }

    public AtomicLong getHeartbeatNum() {
        return heartbeatNum;
    }

    public long getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(long lastReceive) {
        if (this.lastReceiveTime < lastReceive) {
            this.lastReceiveTime = lastReceive;
        }
    }

    public long getLastSentTime() {
        return lastSentTime;
    }

    public void setLastSentTime(long lastSent) {
        if (this.lastSentTime < lastSent) {
            this.lastSentTime = lastSent;
        }
    }

    public double getCpu() {
        return Cpu;
    }

    public void setCpu(double cpu) {
        this.Cpu = cpu;
    }

    @Override
    public String toString() {
        return "CountInfo{" +
                "lastReceiveTime=" + lastReceiveTime +
                ", lastSentTime=" + lastSentTime +
                ", maxChannelNum=" + maxChannelNum +
                ", curChannelNum=" + curChannelNum +
                ", receiveNum=" + receiveNum +
                ", sentNum=" + sentNum +
                ", heartbeatNum=" + heartbeatNum +
                ", Cpu=" + Cpu +
                '}';
    }
}