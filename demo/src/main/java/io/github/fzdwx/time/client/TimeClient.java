package io.github.fzdwx.time.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 22:18
 */
public class TimeClient {

    private final int port;
    private final String host;

    public TimeClient(final int port, final String host) {
        this.port = port;
        this.host = host;
    }

    public TimeClient(final int port) {
        this(port, "localhost");
    }

    public void run() throws InterruptedException {
        final var worker = new NioEventLoopGroup();

        try {
            new Bootstrap()
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    })
                    .connect(host, port).sync()
                    .channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
        }
    }
}