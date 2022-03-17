package io.github.fzdwx.time;

import io.github.fzdwx.discard.DiscardServ;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 22:10
 */
public class TimeServ extends DiscardServ {

    public TimeServ(final int port) {
        super(port);
    }

    @Override
    protected ChannelInitializer<SocketChannel> initChannel() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new TimeEncoder(), new TimeServHandler());
            }
        };
    }

    public static void main(String[] args) throws InterruptedException {
        new TimeServ(8888).run();
    }
}