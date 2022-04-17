package cn.sdutcs.mqtt.panel.controller;

import cn.sdutcs.mqtt.panel.config.ClusterInfo;
import cn.sdutcs.mqtt.panel.model.Metrics;
import cn.sdutcs.mqtt.panel.model.Point;
import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.QpsCounter;
import cn.sdutcs.mqtt.panel.service.RedisService;
import cn.sdutcs.mqtt.panel.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

@RestController
public class MonitorController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private QpsCounter qpsCounter;

    @GetMapping("/cluster/info")
    public Result getClusterInfo() {
        Map brokerConfig = redisService.getBrokerConfig();
        brokerConfig.put("runtime", new ClusterInfo());
        return Result.success(brokerConfig);
    }

    @GetMapping("/cluster/metrics")
    public Result getClusterMetrics() {
        ArrayList<String> pastDateList = DateUtil.getPastDateList(10);
        Metrics brokerMetrics = redisService.getBrokerMetrics(pastDateList);
        return Result.success(brokerMetrics);
    }

    @GetMapping("/server/qps")
    public Result<Object> getQps(@RequestParam(name = "t") String time) {
        long t = Long.parseLong(time);
        Timestamp toTime = new Timestamp(t);
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String tStr = sdf.format(toTime);

        Integer qps = qpsCounter.GetCount("qps", tStr);
        Point point = new Point(time, qps);
        return Result.success(point);
    }
}