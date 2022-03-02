package cn.sdutcs.mqtt.broker.handler;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.broker.service.BlackListService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class IpFilterRuleHandler extends RuleBasedIpFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpFilterRuleHandler.class);

    @Autowired
    private BlackListService blackListService;

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        String hostAddress = remoteAddress.getHostString();
        IpFilterRuleType filterRuleType = IpFilterRuleType.ACCEPT;
        if (StrUtil.isNotBlank(hostAddress)) {
            boolean isReject = blackListService.checkBlackList(hostAddress); // 检测实时黑名单
            if (isReject) {
                filterRuleType = IpFilterRuleType.REJECT; // 拒绝创建
                LOGGER.info("ip = {} is rejected.", hostAddress);
            }
        }
        return filterRuleType == IpFilterRuleType.ACCEPT;
    }
}
