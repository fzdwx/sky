package io.github.fzdwx.inf.socket.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.socket.SocketSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 20:18
 */
public class SocketSessionImpl implements SocketSession {

    private final Channel channel;

    public SocketSessionImpl(final Channel channel) {
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
        return this.channel.writeAndFlush(new TextWebSocketFrame(text));
    }

    @Override
    public ChannelFuture send(final byte[] text) {
        return this.channel.writeAndFlush(new TextWebSocketFrame(Netty.wrap(text)));
    }

    @Override
    public ChannelFuture sendBinary(final byte[] binary) {
        return this.channel.writeAndFlush(new BinaryWebSocketFrame(Netty.wrap(binary)));
    }
}