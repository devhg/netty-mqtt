package cn.sdutcs.mqtt.broker.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 废弃
 */
@ChannelHandler.Sharable
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    public HeartbeatHandler() {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                //      todo 遗嘱
            } else if (e.state() == IdleState.READER_IDLE) {
                // todo 处理遗嘱
                ctx.channel().close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Heartbeat) {
            if (logger.isDebugEnabled()) {
                logger.debug("Heartbeat received.");
            }

            // Server server = ServerContext.getContext().getServer();
            // if (server != null) {
            //     server.getCountInfo().getHeartbeatNum().incrementAndGet();
            // }
            // return;
        }
        super.channelRead(ctx, msg);
    }
}