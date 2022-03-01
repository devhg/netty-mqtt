package cn.sdutcs.mqtt.broker.web.dao;

import cn.sdutcs.mqtt.common.record.Packet;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
