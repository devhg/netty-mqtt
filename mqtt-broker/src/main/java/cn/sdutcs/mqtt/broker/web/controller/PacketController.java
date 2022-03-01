package cn.sdutcs.mqtt.broker.web.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.broker.service.PacketService;
import cn.sdutcs.mqtt.broker.web.dao.BlackListMapper;
import cn.sdutcs.mqtt.broker.web.dao.PacketMapper;
import cn.sdutcs.mqtt.broker.web.model.BlackIP;
import cn.sdutcs.mqtt.broker.web.model.Result;
import cn.sdutcs.mqtt.common.record.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.*;

/**
 * 报文监控 测试通过
 */
@RestController
@RequestMapping("/packet")
public class PacketController {

    @Autowired
    private PacketService packetService;

    @GetMapping("/list")
    public Result<Object> getList(@RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                  @RequestParam(name = "from", required = false) String from,
                                  @RequestParam(name = "to", required = false) String to,
                                  @RequestParam(name = "clientId", required = false) String clientId) {
        System.out.println("clientId = " + clientId);

        if (StrUtil.isBlank(from) || StrUtil.isBlank(to)) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -10);//实现日期加一操作,也就是明天
            from = DateUtil.formatDateTime(calendar.getTime());
            to = DateUtil.now();
        }

        int packetsTotal = packetService.getPacketsTotal(clientId, from, to);
        System.out.println(packetsTotal);
        List<Packet> packets = packetService.fetchPackets(from, to, page, pageSize, clientId);
        Map<String, Object> res = new HashMap<>();
        res.put("data", packets);
        res.put("total", packetsTotal);

        return Result.success(res);
    }
}