package cn.sdutcs.mqtt.panel.controller;

import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.SubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/topic")
public class SubscribeController {
    @Autowired
    private SubscribeService subscribeService;

    @GetMapping("/list")
    public Result getTopicList() {
        return Result.success(subscribeService.getTopicList());
    }
}
