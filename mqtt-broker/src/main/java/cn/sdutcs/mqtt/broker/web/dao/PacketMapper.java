package cn.sdutcs.mqtt.broker.web.dao;

import cn.sdutcs.mqtt.common.record.Packet;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface PacketMapper {
    @Insert("insert into packet_info(packet_type, client_id, topic, packet_info, qos) " +
            "values(#{packetType}, #{clientId}, #{topic}, #{packetInfo}, #{qos})")
    int insertPacket(Packet packet);

    @Select("select * from packet_info where create_time between #{fromTime} and #{toTime}")
    @Results(id = "userMap", value = {
            @Result(id = true, column = "packet_id", property = "packetId"),
            @Result(column = "packet_info", property = "packetInfo"),
            @Result(column = "create_time", property = "createTime")})
    List<Packet> fetchAllPackets(@Param("fromTime") String fromTime,
                                 @Param("toTime") String toTime);
}
