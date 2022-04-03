package cn.sdutcs.mqtt.panel.controller;

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
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/list")
    public Result<Object> getClientList(@RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                        @RequestParam(name = "groupName", required = false) String groupName,
                                        @RequestParam(name = "opUser", required = false) String opUser) {
        Map<String, Object> res = clientService.fetchClientList(page, pageSize, groupName, opUser);
        return Result.success(res);
    }

    @PostMapping("/create")
    public Result<Object> createClient(@RequestBody ClientPo clientPo) {
        System.out.println("clientPo = " + clientPo);
        boolean insertOK = clientService.addNewClient(clientPo);
        return insertOK ? Result.success("客户端注册成功") : Result.failure("客户端注册失败");
    }

    @DeleteMapping(value = "/delete")
    public Result<Object> delete(@RequestParam(name = "id") Long id) {
        boolean deleteOk = clientService.deleteFromClientList(id);
        return deleteOk ? Result.success("删除成功") : Result.failure("删除失败");
    }
}
