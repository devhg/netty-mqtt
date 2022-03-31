package cn.sdutcs.mqtt.panel.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.panel.model.Packet;
import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.service.PacketService;
import cn.sdutcs.mqtt.panel.service.QpsCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 报文监控 测试通过
 */
@RestController
@RequestMapping("/packet")
public class PacketController {

    @Autowired
    private PacketService packetService;
    @Autowired
    private QpsCounter qpsCounter;

    @GetMapping("/list")
    public Result<Object> getList(@RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                  @RequestParam(name = "from", required = false) String from,
                                  @RequestParam(name = "to", required = false) String to,
                                  @RequestParam(name = "clientId", required = false) String clientId) {
        if (StrUtil.isBlank(from) || StrUtil.isBlank(to)) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -10);//实现日期加一操作,也就是明天
            from = DateUtil.formatDateTime(calendar.getTime());
            to = DateUtil.now();
        }

        int packetsTotal = packetService.getPacketsTotal(clientId, from, to);
        List<Packet> packets = packetService.fetchPackets(from, to, page, pageSize, clientId);
        Map<String, Object> res = new HashMap<>();
        res.put("data", packets);
        res.put("total", packetsTotal);

        return Result.success(res);
    }

    @GetMapping("/qps")
    public Result<Object> getQps(@RequestParam(name = "t") String time) {
        long t = Long.parseLong(time);
        Timestamp toTime = new Timestamp(t);
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String tStr = sdf.format(toTime);

        Integer qps = qpsCounter.GetCount("qps", tStr);
        Map<String, Object> data = new HashMap<>();
        data.put("x", time);
        data.put("y", qps);

        return Result.success(data);
    }

    @GetMapping("/packets_per_second")
    public Result<Object> getPacketsPerSecond(@RequestParam(name = "t") String time) {
        Map<String, Object> data = packetService.getPacketsTotal(time);
        return Result.success(data);
    }
}