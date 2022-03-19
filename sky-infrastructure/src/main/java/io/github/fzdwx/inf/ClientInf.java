package io.github.fzdwx.inf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * client based class.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 14:40
 * @since 0.06
 */
public abstract class ClientInf {

    protected final int port;

    protected final String host;

    protected final EventLoopGroup workerGroup;

    protected final Bootstrap bootstrap;

    private io.netty.channel.Channel channel;

    public ClientInf(final int port) {
        this("localhost", port, 0);
    }

    public ClientInf(final String host, final int port, final int workerCnt) {
        this(host, port, new NioEventLoopGroup(workerCnt));
    }

    public ClientInf(final String host, final int port, final EventLoopGroup worker) {
        this.port = port;
        this.host = host;
        this.workerGroup = worker;
        this.bootstrap = new Bootstrap();
    }

    @SneakyThrows
    public void start() {
        this.bootstrap.group(this.workerGroup)
                .channel(this.clientChannelClass())
                .handler(clientHandler());
        this.addClientOptions(this.bootstrap);
        this.channel = this.bootstrap.connect(this.host, this.port).sync().channel();
    }

    public io.netty.channel.Channel channel() {
        return channel;
    }

    public void stop() {
        this.workerGroup.shutdownGracefully();
    }

    @NonNull
    public abstract ChannelInitializer<SocketChannel> clientHandler();

    @NonNull
    public abstract Class<? extends SocketChannel> clientChannelClass();

    public abstract void addClientOptions(final Bootstrap bootstrap);
}