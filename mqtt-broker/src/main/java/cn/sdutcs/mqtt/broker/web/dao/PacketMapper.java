package cn.sdutcs.mqtt.broker.web.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface PacketMapper {
    @Insert("insert into packet_info() values()")
    int insertPacket();
}
