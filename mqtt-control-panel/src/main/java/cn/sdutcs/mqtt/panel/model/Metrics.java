package cn.sdutcs.mqtt.panel.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@Data
public class Metrics {
    private int CurConnNum;
    private int maxConnNum;

    private int reqNum;
    private int reqAvg;
    private ArrayList<Point> reqNumOfDays;

    private int packetNum;
    private int packetAvg;
    private ArrayList<Point> packetNumOfDays;

    private Date lastSentTime;
    private Date lastReceiveTime;

    private String cpu;
}
