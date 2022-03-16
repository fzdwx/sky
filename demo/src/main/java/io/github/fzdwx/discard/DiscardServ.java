package io.github.fzdwx.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/15 21:49
 */
@Slf4j
public class DiscardServ {

    private final int port;
    private final NioEventLoopGroup boss;
    private final NioEventLoopGroup worker;
    private final ServerBootstrap b;

    public DiscardServ(final int port) {
        this(port, 0, 0);
    }

    public DiscardServ(final int port, final int bossThreadNum, final int workerThreadNum) {
        this.port = port;
        this.boss = new NioEventLoopGroup(bossThreadNum);
        this.worker = new NioEventLoopGroup(workerThreadNum);
        this.b = new ServerBootstrap();
    }

    public void run() throws InterruptedException {
        try {
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(initChannel())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // bind port to start serv
                    .bind(port).sync()
                    // close gracefully
                    .channel().closeFuture().sync();
        } finally {
            close();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final var discardServ = new DiscardServ(8888);
        log.error("start");
        discardServ.run();
    }

    protected ChannelInitializer<SocketChannel> initChannel() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new DiscardHandler());
            }
        };
    }

    private void close() {
        this.boss.shutdownGracefully();
        this.worker.shutdownGracefully();
    }
}