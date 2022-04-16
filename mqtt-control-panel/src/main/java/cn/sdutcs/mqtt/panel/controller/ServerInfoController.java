package cn.sdutcs.mqtt.panel.controller;

import cn.sdutcs.mqtt.panel.config.ClusterInfo;
import cn.sdutcs.mqtt.panel.model.Metrics;
import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.RedisService;
import cn.sdutcs.mqtt.panel.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class ServerInfoController {

    @Autowired
    private RedisService redisService;

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
}