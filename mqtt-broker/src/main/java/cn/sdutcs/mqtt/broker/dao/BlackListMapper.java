package cn.sdutcs.mqtt.broker.dao;

import cn.sdutcs.mqtt.broker.domain.BlackIP;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlackListMapper {
    List<BlackIP> getAll(@Param("ip") String ip, @Param("opUser") String opUser);

    BlackIP getOne(String hostAddress);
}
