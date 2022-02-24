package cn.sdutcs.mqtt.broker.web.controller;

import cn.sdutcs.mqtt.broker.web.dao.BlackListMapper;
import cn.sdutcs.mqtt.broker.web.dao.UserMapper;
import cn.sdutcs.mqtt.broker.web.model.BlackIP;
import cn.sdutcs.mqtt.broker.web.model.Result;
import cn.sdutcs.mqtt.broker.web.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IP黑名单管理 测试通过
 */
@RestController
public class BlackIPController {

    @Autowired
    private BlackListMapper blackListMapper;

    @GetMapping("/blacklist")
    public Result<Object> getBlackIPList() {
        List<BlackIP> blackIPList = blackListMapper.getAll();
        return Result.success(blackIPList);
    }

    @PostMapping("/blackip")
    public Result<Object> createBlackIP(@RequestBody BlackIP blackIP) {
        int insertOK = blackListMapper.insert(blackIP);
        System.out.println("insertOK = " + insertOK);
        if (insertOK == 1) {
            return Result.SUCCESS;
        }
        return Result.failure("加入黑名单失败");
    }

    @DeleteMapping(value = "/delete/{id}")
    public Result<Object> delete(@PathVariable("id") Long id) {
        int delete = blackListMapper.delete(id);
        if (delete == 1) {
            return Result.SUCCESS;
        }
        return Result.FAIL;
    }
}