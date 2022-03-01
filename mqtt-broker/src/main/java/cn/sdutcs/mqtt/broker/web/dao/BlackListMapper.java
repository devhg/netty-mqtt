package cn.sdutcs.mqtt.broker.web.dao;

import cn.sdutcs.mqtt.broker.web.model.BlackIP;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlackListMapper {
    List<BlackIP> getAll(@Param("ip") String ip, @Param("opUser") String opUser);

    int insert(BlackIP ip);

    int update(BlackIP ip);

    int delete(Long id);

    BlackIP getOne(String hostAddress);
}
