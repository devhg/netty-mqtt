package cn.sdutcs.mqtt.broker.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class CountHandler extends ChannelOutboundHandlerAdapter {

    @Autowired
    private CountInfo countInfo;

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        countInfo.getSentNum().incrementAndGet();
        countInfo.setLastSentTime(System.currentTimeMillis());
    }
}
