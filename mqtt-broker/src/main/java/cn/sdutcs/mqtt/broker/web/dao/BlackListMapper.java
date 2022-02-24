package cn.sdutcs.mqtt.broker.web.dao;

import cn.sdutcs.mqtt.broker.web.model.BlackIP;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlackListMapper {
    List<BlackIP> getAll();

    int insert(BlackIP ip);

    int delete(Long id);

    int exist(String hostAddress);
}
