package io.github.fzdwx.inf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * client based class.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 14:40
 * @since 0.06
 */
public abstract class ClientInf<Client> extends ServAndClientBase<Client> {

    protected final int port;
    protected final String host;
    protected final EventLoopGroup workerGroup;
    protected final Bootstrap bootstrap;

    protected BufferedReader scanner;
    private Channel ch;

    public ClientInf(final int port) {
        this("localhost", port, 0);
    }

    public ClientInf(final String host, final int port) {
        this(host, port, 0);
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

    @Override
    public Channel channel() {
        return this.ch;
    }

    @Override
    public ChannelFuture bind(final InetSocketAddress address) {
        this.bootstrap.group(this.workerGroup)
                .channel(this.channelClassType())
                .handler(channelInitializer());
        this.addClientOptions(this.bootstrap);
        final var bindFuture = this.bootstrap.connect(this.host, this.port);
        this.ch = bindFuture.channel();

        return bindFuture;
    }

    @Override
    public ChannelFuture bind() {
        return bind(new InetSocketAddress(host, port));
    }

    @Override
    public int port() {
        return port;
    }

    @SneakyThrows
    public void scanner(InputStream in) {
        System.out.println("Enter your commands (quit to end)");

        ChannelFuture lastWriteFuture = null;
        scanner = new BufferedReader(new InputStreamReader(in));
        for (; ; ) {
            final String input = scanner.readLine();
            final String line = input != null ? input.trim() : null;
            if (line == null || "quit".equalsIgnoreCase(line)) { // EOF or "quit"
                ch.close().sync();
                break;
            } else if (line.isEmpty()) { // skip `enter` or `enter` with spaces.
                continue;
            }
            // Sends the received line to the server.
            lastWriteFuture = ch.writeAndFlush(line);
            lastWriteFuture.addListener((GenericFutureListener<ChannelFuture>) future -> {
                if (!future.isSuccess()) {
                    System.err.print("write failed: ");
                    future.cause().printStackTrace(System.err);
                }
            });
        }

        if (lastWriteFuture != null) {
            lastWriteFuture.sync();
        }

        stop();
    }

    public void scanner() {
        scanner(System.in);
    }

    public Class<? extends SocketChannel> channelClassType() {
        return NioSocketChannel.class;
    }

    public void stop() {
        this.workerGroup.shutdownGracefully();
    }

    public abstract void addClientOptions(final Bootstrap bootstrap);
}