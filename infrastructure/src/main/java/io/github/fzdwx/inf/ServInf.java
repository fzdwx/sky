package io.github.fzdwx.inf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 14:40
 */
public abstract class ServInf {

    protected final int port;

    protected final EventLoopGroup bossGroup;

    protected final EventLoopGroup workerGroup;

    protected final ServerBootstrap serverBootstrap;

    private io.netty.channel.Channel channel;

    public ServInf(final int port) {
        this(port, 0, 0);
    }

    public ServInf(final int port, final int bossCnt, final int workerCnt) {
        this(port, new NioEventLoopGroup(bossCnt), new NioEventLoopGroup(workerCnt));
    }

    public ServInf(final int port, final EventLoopGroup boss, final EventLoopGroup worker) {
        this.port = port;
        this.bossGroup = boss;
        this.workerGroup = worker;
        this.serverBootstrap = new ServerBootstrap();
    }

    @SneakyThrows
    public void start() {
        this.serverBootstrap.group(this.bossGroup, this.workerGroup)
                .channel(this.serverChannelClass());
        final var channelHandler = servHandlers();
        if (channelHandler != null) {
            this.serverBootstrap.handler(servHandlers());
        }
        this.addServOptions();
        this.channel = this.serverBootstrap.childHandler(this.addChildHandler())
                .bind(this.port).sync()
                .channel();
        this.channel.closeFuture().sync();
    }

    public void stop() {
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }

    public io.netty.channel.Channel channel() {
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
}