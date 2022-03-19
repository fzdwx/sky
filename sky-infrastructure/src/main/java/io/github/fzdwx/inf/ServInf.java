package io.github.fzdwx.inf;

import io.github.fzdwx.lambada.internal.PrintUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * server based class.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 14:40
 * @since 0.06
 */
@Slf4j
public abstract class ServInf {

    protected final int port;
    protected String name;
    protected int bossCnt = 0;
    protected EventLoopGroup bossGroup;

    protected int workerCnt = 0;
    protected EventLoopGroup workerGroup;

    protected ServerBootstrap serverBootstrap;

    private Channel channel;

    public ServInf(final int port) {
        this("serv", port);
    }

    public ServInf(final String name, final int port) {
        this.name = name;
        this.port = port;
    }

    public ServInf workerCnt(int workerCnt) {
        this.workerCnt = workerCnt;
        return this;
    }

    public ServInf bossCnt(int bossCnt) {
        this.bossCnt = bossCnt;
        return this;
    }

    public ServInf bossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
        return this;
    }

    public ServInf workerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
        return this;
    }

    public ServInf name(String name) {
        this.name = name;
        return this;
    }

    @SneakyThrows
    public void start() {
        if (bossGroup == null) {
            this.bossGroup = new NioEventLoopGroup(this.bossCnt);
        }
        if (workerGroup == null) {
            this.workerGroup = new NioEventLoopGroup(this.workerCnt);
        }

        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.group(this.bossGroup, this.workerGroup)
                .channel(this.serverChannelClass());
        final var channelHandler = servHandlers();
        if (channelHandler != null) {
            this.serverBootstrap.handler(servHandlers());
        }
        this.addServOptions();
        this.channel = this.serverBootstrap.childHandler(this.addChildHandler())
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

    public ChannelHandler servHandlers() {
        return null;
    }

    @NonNull
    public abstract Class<? extends ServerChannel> serverChannelClass();

    public abstract void addServOptions();

    protected void onStart(final Future<? super Void> f) {
        log.info(this.name + " start at port: " + this.port);
    }
}