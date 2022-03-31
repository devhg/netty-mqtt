package cn.sdutcs.mqtt.panel.controller;

import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.ISessionStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private ISessionStoreService sessionStoreService;

    @GetMapping("/list")
    public Result<Object> getBlackIPList(@RequestParam(name = "ip", required = false) String ip,
                                         @RequestParam(name = "opUser", required = false) String opUser) {
        // List<BlackIP> blackIPList = blackListService.fetchIPBlackList(ip, opUser);
        List<Map> sessionList = sessionStoreService.getSessionList();
        return Result.success(sessionList);
    }

    @DeleteMapping(value = "/logout")
    public Result<Object> logout(@RequestParam(name = "ip", required = false) String clientId) {
        sessionStoreService.remove(clientId);
        boolean deleteOk = true;
        return deleteOk ? Result.success("下线成功") : Result.failure("下线失败");
    }
}
