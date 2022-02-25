package cn.sdutcs.mqtt.broker.service;

import cn.sdutcs.mqtt.broker.handler.BrokerHandler;
import cn.sdutcs.mqtt.broker.web.dao.PacketMapper;
import cn.sdutcs.mqtt.common.record.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Packet> fetchPackets(String fromTime, String toTime) {
        return packetMapper.fetchAllPackets(fromTime, toTime);
    }
}
