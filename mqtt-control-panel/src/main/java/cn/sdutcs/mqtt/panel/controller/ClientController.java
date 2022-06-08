package cn.sdutcs.mqtt.panel.controller;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.common.auth.IAuthService;
import cn.sdutcs.mqtt.panel.model.BlackIP;
import cn.sdutcs.mqtt.panel.model.ClientPo;
import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.BlackListService;
import cn.sdutcs.mqtt.panel.service.ClientService;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 客户端管理
 */
@RestController
@CrossOrigin
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;
    @Autowired
    private IAuthService authService;

    @GetMapping("/list")
    public Result<Object> getClientList(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                                        @RequestParam(name = "groupName", required = false) String groupName,
                                        @RequestParam(name = "opUser", required = false) String opUser) {
        if (StrUtil.isBlank(groupName)) {
            groupName = null;
        }
        if (StrUtil.isBlank(opUser)) {
            opUser = null;
        }

        Map<String, Object> res = clientService.fetchClientList(page, pageSize, groupName, opUser);
        return Result.success(res);
    }

    @GetMapping(value = "/loginPwd")
    public Result<Object> getClientPasswdByClientName(@RequestParam(name = "clientName") String clientName) {
        String key = authService.genPassword(clientName);
        return Result.success(key);
    }

    @PostMapping("/create")
    public Result<Object> createClient(@RequestBody ClientPo clientPo) {
        boolean insertOK = false;
        try {
            insertOK = clientService.addNewClient(clientPo);
        } catch (Exception e) {
            // e.printStackTrace();
            return Result.failure("注册失败，此客户端名称已经存在");
        }
        return insertOK ? Result.success("客户端注册成功") : Result.failure("客户端注册失败");
    }

    @DeleteMapping(value = "/delete")
    public Result<Object> deleteClient(@RequestParam(name = "id") Long id) {
        boolean deleteOk = clientService.deleteFromClientList(id);
        return deleteOk ? Result.success("删除成功") : Result.failure("删除失败");
    }
}
