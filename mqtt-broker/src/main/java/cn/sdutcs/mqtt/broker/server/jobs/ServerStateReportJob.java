package cn.sdutcs.mqtt.broker.server.jobs;

import cn.sdutcs.mqtt.broker.handler.CountInfo;
import cn.sdutcs.mqtt.broker.service.StatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServerStateReportJob {

    private static final Logger logger = LoggerFactory.getLogger(ServerStateReportJob.class);

    @Autowired
    private StatService statService;
    @Autowired
    private CountInfo countInfo;

    @Scheduled(fixedRate = 5000)
    public void scheduledTask() {
        statService.storeBrokerCountInfo(countInfo);
    }
}