package io.github.fzdwx.echo;

import io.github.fzdwx.discard.DiscardServ;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 21:51
 */
public class EchoServ extends DiscardServ {

    public EchoServ(final int port) {
        super(port);
    }

    @Override
    protected ChannelInitializer<SocketChannel> initChannel() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new EchoHandler());
            }
        };
    }
}