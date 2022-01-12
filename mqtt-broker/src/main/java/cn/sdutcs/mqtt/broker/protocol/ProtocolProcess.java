package cn.sdutcs.mqtt.broker.protocol;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.internal.InternalCommunication;
import cn.sdutcs.mqtt.broker.service.*;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


/**
 * 协议处理
 */
@Component
public class ProtocolProcess {

    @Autowired
    private BrokerConfig brokerProperties;

    @Autowired
    private ChannelGroup channelGroup;

    @Autowired
    private ConcurrentHashMap<String, ChannelId> channelIdMap;

    private IAuthService authService;
    private ISessionStoreService sessionStoreService;
    private IMessageIdService messageIdService;
    private IRetainMessageStoreService retainMessageStoreService;
    private ISubscribeStoreService subscribeStoreService;
    private IDupPublishMessageStoreService dupPublishMessageStoreService;
    private IDupPubRelMessageStoreService dupPubRelMessageStoreService;

    private InternalCommunication internalCommunication;

    private Connect connect;

    private Subscribe subscribe;

    private UnSubscribe unSubscribe;

    private Publish publish;

    private DisConnect disConnect;

    private PingReq pingReq;

    private PubRel pubRel;

    private PubAck pubAck;

    private PubRec pubRec;

    private PubComp pubComp;

    public Connect connect() {
        if (connect == null) {
            connect = new Connect(brokerProperties, authService, sessionStoreService,
                    subscribeStoreService, dupPublishMessageStoreService,
                    dupPubRelMessageStoreService, channelIdMap);
        }
        return connect;
    }

    public Subscribe subscribe() {
        if (subscribe == null) {
            subscribe = new Subscribe(messageIdService, retainMessageStoreService, subscribeStoreService);
        }
        return subscribe;
    }

    public UnSubscribe unSubscribe() {
        if (unSubscribe == null) {
            unSubscribe = new UnSubscribe(subscribeStoreService);
        }
        return unSubscribe;
    }

    public Publish publish() {
        if (publish == null) {
            publish = new Publish(
                    brokerProperties, channelIdMap, channelGroup,
                    internalCommunication, sessionStoreService, subscribeStoreService, messageIdService,
                    retainMessageStoreService, dupPublishMessageStoreService);
        }
        return publish;
    }

    public DisConnect disConnect() {
        if (disConnect == null) {
            disConnect = new DisConnect(sessionStoreService, subscribeStoreService,
                    dupPublishMessageStoreService, dupPubRelMessageStoreService);
        }
        return disConnect;
    }

    public PingReq pingReq() {
        if (pingReq == null) {
            pingReq = new PingReq(brokerProperties, channelIdMap, sessionStoreService);
        }
        return pingReq;
    }

    public PubRel pubRel() {
        if (pubRel == null) {
            pubRel = new PubRel();
        }
        return pubRel;
    }

    public PubAck pubAck() {
        if (pubAck == null) {
            pubAck = new PubAck(dupPublishMessageStoreService);
        }
        return pubAck;
    }

    public PubRec pubRec() {
        if (pubRec == null) {
            pubRec = new PubRec(dupPublishMessageStoreService, dupPubRelMessageStoreService);
        }
        return pubRec;
    }

    public PubComp pubComp() {
        if (pubComp == null) {
            pubComp = new PubComp(dupPubRelMessageStoreService);
        }
        return pubComp;
    }

    // public ISessionStoreService getSessionStoreService() {
    //     return sessionStoreService;
    // }
}
