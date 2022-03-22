package io.github.fzdwx.inf;

import io.github.fzdwx.lambada.internal.PrintUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * server based class.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 14:40
 * @since 0.06
 */
@Slf4j
public abstract class ServInf<Serv> extends ServAndClientBase<Serv> {

    private final Map<ChannelOption<?>, Object> servOptions = new HashMap();
    private final Map<ChannelOption<?>, Object> childOptions = new HashMap();
    protected int port;
    protected String name;
    protected int bossCnt = 0;
    protected EventLoopGroup bossGroup;
    protected int workerCnt = 0;
    protected EventLoopGroup workerGroup;
    protected ServerBootstrap serverBootstrap;
    private LoggingHandler logging;
    private Channel channel;
    private Boolean ssl;

    public ServInf(final int port) {
        this("serv", port);
    }

    public ServInf(final String name, final int port) {
        this.name = name;
        this.port = port;
    }

    /* init options start */
    public Serv workerCnt(int workerCnt) {
        this.workerCnt = workerCnt;
        return me();
    }

    public Serv bossCnt(int bossCnt) {
        this.bossCnt = bossCnt;
        return me();
    }

    public Serv bossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
        return me();
    }

    public Serv workerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
        return me();
    }

    public Serv name(String name) {
        this.name = name;
        return me();
    }

    public <T> Serv servOptions(ChannelOption<T> option, T t) {
        servOptions.put(option, t);
        return me();
    }

    public <T> Serv childOptions(ChannelOption<T> option, T t) {
        childOptions.put(option, t);
        return me();
    }
    /* init options end */

    public Channel channel() {
        return channel;
    }

    public Boolean ssl() {
        if (this.ssl == null) {
            this.ssl = channel.pipeline().get(SslHandler.class) != null;
        }
        return ssl;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public ChannelFuture bind(final InetSocketAddress address) {
        init();

        final var bindFuture = this.serverBootstrap
                .childHandler(channelInitializer())
                .bind(this.port);

        this.channel = bindFuture.channel();

        bindFuture.addListener(f -> {
            if (bindFuture.isSuccess()) {
                PrintUtil.printBanner();

                this.onStartSuccess();
            }
        });
        return bindFuture;
    }

    public void stop() {
        if (!this.workerGroup.isShutdown()) {
            this.workerGroup.shutdownGracefully();
        }
        if (!this.bossGroup.isShutdown()) {
            this.bossGroup.shutdownGracefully();
        }
    }

    protected void onStartSuccess() {
        log.info(this.name + " start at port: " + this.port);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    protected void init() {
        if (bossGroup == null) {
            this.bossGroup = new NioEventLoopGroup(this.bossCnt);
        }
        if (workerGroup == null) {
            this.workerGroup = new NioEventLoopGroup(this.workerCnt);
        }

        this.serverBootstrap = new ServerBootstrap()
                .group(this.bossGroup, this.workerGroup)
                .channel(this.channelClassType());


        if (logging != null) {
            this.serverBootstrap = this.serverBootstrap.handler(logging);
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : servOptions.entrySet()) {
            this.serverBootstrap = serverBootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.serverBootstrap = serverBootstrap.childOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }
    }

    public Class<? extends ServerChannel> channelClassType() {
        return NioServerSocketChannel.class;
    }
}