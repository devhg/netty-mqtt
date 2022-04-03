package cn.sdutcs.mqtt.panel.dao;

import cn.sdutcs.mqtt.panel.model.Packet;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PacketMapper {

    int insertPacket(Packet packet);

    List<Packet> fetchAllPackets(@Param("fromTime") String fromTime,
                                 @Param("toTime") String toTime,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset,
                                 @Param("clientId") String clientId);

    int getPacketsTotal(@Param("clientId") String clientId,
                        @Param("fromTime") String fromTime,
                        @Param("toTime") String toTime);

    @MapKey("x")
    Map<String, Object> getPacketsSumPerSecond(@Param("fromTime") String fromTime,
                                               @Param("toTime") String toTime);
}
