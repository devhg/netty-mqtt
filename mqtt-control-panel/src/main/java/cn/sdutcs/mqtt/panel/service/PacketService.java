package cn.sdutcs.mqtt.panel.service;

import cn.sdutcs.mqtt.panel.dao.PacketMapper;
import cn.sdutcs.mqtt.panel.model.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Service
public class PacketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacketService.class);

    @Autowired
    PacketMapper packetMapper;

    public void Log(String packetType, String clientID, String topic, String packetInfo, String qos) {
        Packet packet = new Packet(packetType, clientID, topic, packetInfo, qos);
        boolean ok = this.createPacketRecord(packet);
        if (!ok) {
            LOGGER.error("create Packet record error!");
        }
    }

    public boolean createPacketRecord(Packet packet) {
        int ok = packetMapper.insertPacket(packet);
        return ok == 1;
    }

    public List<Packet> fetchPackets(String fromTime, String toTime,
                                     int page, int pageSize, String clientId) {
        return packetMapper.fetchAllPackets(fromTime, toTime, pageSize, (page - 1) * pageSize, clientId);
    }

    public int getPacketsTotal(String clientId, String fromTime, String toTime) {
        return packetMapper.getPacketsTotal(clientId, fromTime, toTime);
    }

    public Map<String, Object> getPacketsSumPerSecond(String time) {
        long t = Long.parseLong(time);
        Timestamp toTime = new Timestamp(t);
        Timestamp fromTime = new Timestamp(t - 2000);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String fromStr = sdf.format(fromTime);
        String toStr = sdf.format(toTime);

        return packetMapper.getPacketsSumPerSecond(fromStr, toStr);
    }
}
