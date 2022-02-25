package cn.sdutcs.mqtt.broker.web.controller;

import cn.sdutcs.mqtt.broker.service.PacketService;
import cn.sdutcs.mqtt.broker.web.dao.BlackListMapper;
import cn.sdutcs.mqtt.broker.web.dao.PacketMapper;
import cn.sdutcs.mqtt.broker.web.model.BlackIP;
import cn.sdutcs.mqtt.broker.web.model.Result;
import cn.sdutcs.mqtt.common.record.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

/**
 * 报文监控 测试通过
 */
@RestController
public class PacketController {

    @Autowired
    private PacketService packetService;

    @GetMapping("/packets")
    public Result<Object> getList(@PathParam("from") String from,
                                  @PathParam("to") String to) {
        List<Packet> packets = packetService.fetchPackets(from, to);
        return Result.success(packets);
    }
}