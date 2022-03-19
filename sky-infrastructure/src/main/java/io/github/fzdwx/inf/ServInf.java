package io.github.fzdwx.inf;

import io.github.fzdwx.lambada.internal.PrintUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
public abstract class ServInf<Serv extends ServInf<Serv>> {

    protected final int port;
    private final Map<ChannelOption<?>, Object> servOptions = new HashMap();
    private final Map<ChannelOption<?>, Object> childOptions = new HashMap();
    protected String name;
    protected int bossCnt = 0;
    protected EventLoopGroup bossGroup;
    protected int workerCnt = 0;
    protected EventLoopGroup workerGroup;
    protected ServerBootstrap serverBootstrap;
    private LoggingHandler logging;
    private Channel channel;

    public ServInf(final int port) {
        this("serv", port);
    }

    public ServInf(final String name, final int port) {
        this.name = name;
        this.port = port;
    }

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

    public Serv log(LogLevel level) {
        logging = new LoggingHandler(level);
        return me();
    }

    public Serv log(LogLevel level, ByteBufFormat format) {
        logging = new LoggingHandler(level, format);
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

    @SneakyThrows
    public void bind() {
        init();

        this.channel = this.serverBootstrap.childHandler(addChildHandler())
                .bind(this.port).sync().addListener(f -> {
                    PrintUtil.printBanner();
                    this.onStart(f);
                })
                .channel();
        this.channel.closeFuture().sync();
    }

    public void stop() {
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }

    public Channel channel() {
        return channel;
    }

    @NonNull
    public abstract ChannelInitializer<SocketChannel> addChildHandler();

    @NonNull
    public abstract Class<? extends ServerChannel> serverChannelClass();

    protected void init() {
        if (bossGroup == null) {
            this.bossGroup = new NioEventLoopGroup(this.bossCnt);
        }
        if (workerGroup == null) {
            this.workerGroup = new NioEventLoopGroup(this.workerCnt);
        }

        this.serverBootstrap = new ServerBootstrap()
                .group(this.bossGroup, this.workerGroup)
                .channel(this.serverChannelClass());


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

    protected void onStart(final Future<? super Void> f) {
        log.info(this.name + " start at port: " + this.port);
    }

    protected abstract Serv me();
}