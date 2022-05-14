package cn.sdutcs.mqtt.panel.controller;

import cn.sdutcs.mqtt.panel.model.BlackIP;
import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.BlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IP黑名单管理 测试通过
 */
@RestController
@RequestMapping("/blackip")
public class BlackIPController {

    @Autowired
    private BlackListService blackListService;

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_XHTML_XML_VALUE)
    public Result<Object> getBlackIPList(@RequestParam(name = "ip", required = false) String ip,
                                         @RequestParam(name = "opUser", required = false) String opUser) {
        List<BlackIP> blackIPList = blackListService.fetchIPBlackList(ip, opUser);
        return Result.success(blackIPList);
    }

    @PostMapping("/create")
    public Result<Object> createBlackIP(@RequestBody BlackIP blackIP) {
        // todo 检查用户是否存在
        boolean insertOK = blackListService.addIPToBlackList(blackIP);
        return insertOK ? Result.success("加入黑名单成功") : Result.failure("加入黑名单失败");
    }

    @PostMapping("/update")
    public Result<Object> updateBlackIP(@RequestBody BlackIP blackIP) {
        boolean updateOK = blackListService.updateIPToBlackList(blackIP);
        return updateOK ? Result.success("修改成功") : Result.failure("修改失败");
    }

    @DeleteMapping(value = "/delete")
    public Result<Object> deleteBlackIP(@RequestBody BlackIP blackIP) {
        boolean deleteOk = blackListService.deleteIPFromBlackList(blackIP.getId());
        return deleteOk ? Result.success("删除成功") : Result.failure("删除失败");
    }
}