package cn.sdutcs.mqtt.panel.dao;

import cn.sdutcs.mqtt.panel.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {
    List<User> getAll();

    User validLogin(@Param("username") String username, @Param("password") String password);

    User getOne(Long id);

    void insert(User user);

    void update(User user);

    void delete(Long id);
}