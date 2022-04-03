package cn.sdutcs.mqtt.panel.dao;

import cn.sdutcs.mqtt.panel.model.ClientPo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientMapper {
    List<ClientPo> fetchAllClients(@Param("limit") int limit,
                                   @Param("offset") int offset,
                                   @Param("groupName") String groupName,
                                   @Param("opUser") String opUser);

    int countClientTotal(@Param("groupName") String groupName,
                         @Param("opUser") String opUser);

    ClientPo getClientByName(@Param("clientName") String clientName);

    int insert(ClientPo client);

    int updateClientIdByName(@Param("clientName") String clientName,
                             @Param("clientId") String clientId);

    int deleteClientById(Long id);
}
