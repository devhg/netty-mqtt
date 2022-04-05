package cn.sdutcs.mqtt.panel.controller;

import cn.sdutcs.mqtt.panel.config.ClusterInfo;
import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}