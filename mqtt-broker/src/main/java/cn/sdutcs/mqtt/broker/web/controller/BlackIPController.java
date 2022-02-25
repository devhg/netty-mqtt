package cn.sdutcs.mqtt.broker.web.controller;

import cn.sdutcs.mqtt.broker.service.BlackListService;
import cn.sdutcs.mqtt.broker.web.model.BlackIP;
import cn.sdutcs.mqtt.broker.web.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IP黑名单管理 测试通过
 */
@RestController
public class BlackIPController {

    @Autowired
    private BlackListService blackListService;

    @GetMapping("/blacklist")
    public Result<Object> getBlackIPList() {
        List<BlackIP> blackIPList = blackListService.fetchIPBlackList();
        return Result.success(blackIPList);
    }

    @PostMapping("/blackip")
    public Result<Object> createBlackIP(@RequestBody BlackIP blackIP) {
        boolean insertOK = blackListService.addIPToBlackList(blackIP);
        if (insertOK) {
            return Result.SUCCESS;
        }
        return Result.failure("加入黑名单失败");
    }

    @DeleteMapping(value = "/delete/{id}")
    public Result<Object> delete(@PathVariable("id") Long id) {
        boolean delete = blackListService.deleteIPFromBlackList(id);
        return delete ? Result.SUCCESS : Result.FAIL;
    }
}