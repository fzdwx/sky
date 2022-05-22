package socket.inter;

import core.Netty;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import socket.Socket;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 20:18
 */
public class SocketImpl implements Socket {

    private final Channel channel;

    public SocketImpl(final Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel channel() {
        return this.channel;
    }

    @Override
    public ChannelFuture reject() {
        return this.channel.close();
    }

    @Override
    public ChannelFuture reject(final String text) {
        return this.send(text).addListener(future -> this.reject());
    }

    @Override
    public ChannelFuture send(final String text) {
        return this.channel.writeAndFlush(text.getBytes());
    }

    @Override
    public ChannelFuture send(final byte[] text) {
        return this.channel.writeAndFlush(Netty.wrap(channel.alloc(), text));
    }
}