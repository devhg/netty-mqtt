package cn.sdutcs.mqtt.broker.web.dao;

import cn.sdutcs.mqtt.broker.web.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {
    List<User> getAll();

    User getOne(Long id);

    void insert(User user);

    void update(User user);

    void delete(Long id);
}