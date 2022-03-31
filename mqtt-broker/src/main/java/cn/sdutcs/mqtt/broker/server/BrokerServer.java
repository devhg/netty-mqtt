package cn.sdutcs.mqtt.broker.server;

import cn.hutool.core.util.StrUtil;
import cn.sdutcs.mqtt.broker.codec.MqttWebSocketCodec;
import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.handler.BrokerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BrokerServer implements Lifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerServer.class);
    private volatile boolean running = false;

    @Autowired
    BrokerConfig brokerProperties;
    @Autowired
    ApplicationContext context;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ChannelGroup channelGroup;
    private ConcurrentHashMap<String, ChannelId> channelIdMap;

    private SslContext sslContext;

    private Channel channel;
    private Channel websocketChannel;

    public BrokerServer() {
        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        channelIdMap = new ConcurrentHashMap<>();
        bossGroup = useEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        workerGroup = useEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    @SneakyThrows
    @Override
    public void start() {
        LOGGER.info("Initializing {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        LOGGER.debug("debug");
        LOGGER.warn("warn");
        LOGGER.error("error");

        // 开启SSL
        if (brokerProperties.getSslEnabled()) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("keystore/server.p12");
            keyStore.load(inputStream, brokerProperties.getSslPassword().toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, brokerProperties.getSslPassword().toCharArray());
            sslContext = SslContextBuilder.forServer(kmf).build();
        }

        runMqttServer();
        if (brokerProperties.getWsEnabled()) {
            runWebSocketServer();
            LOGGER.info("MQTT Broker {} is up and running. Open Port: {} WebSocketPort: {}",
                    "[" + brokerProperties.getId() + "]",
                    brokerProperties.getPort(),
                    brokerProperties.getWsPort());
        } else {
            LOGGER.info("MQTT Broker {} is up and running. Open Port: {} ",
                    "[" + brokerProperties.getId() + "]",
                    brokerProperties.getPort());
        }
        running = true;
    }

    @Override
    public void stop() {
        LOGGER.info("Shutdown {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        channelGroup = null;
        channelIdMap = null;
        bossGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup.shutdownGracefully();
        workerGroup = null;
        channel.closeFuture().syncUninterruptibly();
        channel = null;
        if (brokerProperties.getWsEnabled()) {
            websocketChannel.closeFuture().syncUninterruptibly();
            websocketChannel = null;
        }
        running = false;
        LOGGER.info("MQTT Broker {} shutdown finish.", "[" + brokerProperties.getId() + "]");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Bean(name = "channelGroup")
    public ChannelGroup getChannels() {
        return this.channelGroup;
    }

    @Bean(name = "channelIdMap")
    public ConcurrentHashMap<String, ChannelId> getChannelIdMap() {
        return this.channelIdMap;
    }

    public boolean useEpoll() {
        String osName = System.getProperty("os.name");
        boolean isLinuxPlatform = StrUtil.containsIgnoreCase(osName, "linux");
        return isLinuxPlatform && Epoll.isAvailable();
    }

    private void runMqttServer() throws Exception {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                // handler在初始化时就会执行
                .handler(new LoggingHandler(LogLevel.INFO))
                // childHandler会在客户端成功connect后才执行
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 流量监控
                        // pipeline.addLast(TrafficHandler.trafficHandler);
                        // Netty提供的心跳检测
                        pipeline.addFirst("idle", new IdleStateHandler(brokerProperties.getKeepAlive(), 0, 0));

                        // Netty提供的SSL处理
                        if (brokerProperties.getSslEnabled()) {
                            SSLEngine sslEngine = sslContext.newEngine(socketChannel.alloc());
                            sslEngine.setUseClientMode(false);         // 服务端模式
                            sslEngine.setNeedClientAuth(false);        // 不需要验证客户端
                            pipeline.addLast("ssl", new SslHandler(sslEngine));
                        }
                        pipeline.addLast("decoder", new MqttDecoder());
                        pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        pipeline.addLast("broker", context.getBean(BrokerHandler.class));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.getSoKeepAlive());

        if (StrUtil.isNotBlank(brokerProperties.getHost())) {
            channel = sb.bind(brokerProperties.getHost(), brokerProperties.getPort()).sync().channel();
        } else {
            channel = sb.bind(brokerProperties.getPort()).sync().channel();
        }
    }

    private void runWebSocketServer() throws Exception {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        // Netty提供的心跳检测
                        pipeline.addFirst("idle", new IdleStateHandler(brokerProperties.getKeepAlive(), 0, 0));

                        // Netty提供的SSL处理
                        if (brokerProperties.getSslEnabled()) {
                            SSLEngine sslEngine = sslContext.newEngine(socketChannel.alloc());
                            sslEngine.setUseClientMode(false);        // 服务端模式
                            sslEngine.setNeedClientAuth(false);       // 不需要验证客户端
                            pipeline.addLast("ssl", new SslHandler(sslEngine));
                        }

                        // 将请求和应答消息编码或解码为HTTP消息
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        // 将HTTP消息的多个部分合成一条完整的HTTP消息
                        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                        // 将HTTP消息进行压缩编码
                        pipeline.addLast("compressor", new HttpContentCompressor());
                        pipeline.addLast("protocol", new WebSocketServerProtocolHandler(brokerProperties.getWebsocketPath(), "mqtt,mqttv3.1,mqttv3.1.1", true, 65536));
                        pipeline.addLast("mqttWebSocket", new MqttWebSocketCodec());
                        pipeline.addLast("decoder", new MqttDecoder());
                        pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        pipeline.addLast("broker", context.getBean(BrokerHandler.class));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.getSoKeepAlive());

        if (StrUtil.isNotBlank(brokerProperties.getHost())) {
            websocketChannel = sb.bind(brokerProperties.getHost(), brokerProperties.getWsPort()).sync().channel();
        } else {
            websocketChannel = sb.bind(brokerProperties.getWsPort()).sync().channel();
        }
    }
}
