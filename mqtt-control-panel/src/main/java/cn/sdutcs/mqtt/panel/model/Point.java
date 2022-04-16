package cn.sdutcs.mqtt.panel.model;

import lombok.Data;

@Data
public class Point {
    private String x;
    private Integer y;

    public Point(String date, Integer req) {
        this.x = date;
        this.y = req;
    }
}
