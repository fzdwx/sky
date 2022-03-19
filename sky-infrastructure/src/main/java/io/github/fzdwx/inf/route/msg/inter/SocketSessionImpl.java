package io.github.fzdwx.inf.route.msg.inter;

import io.github.fzdwx.inf.route.msg.SocketSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static io.github.fzdwx.inf.Netty.alloc;

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
    public ChannelFuture send(final String text) {
        return this.channel.writeAndFlush(new TextWebSocketFrame(text));
    }

    @Override
    public ChannelFuture send(final byte[] text) {
        return this.channel.writeAndFlush(new TextWebSocketFrame(alloc(text)));
    }

    @Override
    public ChannelFuture sendBinary(final byte[] binary) {
        return this.channel.writeAndFlush(new BinaryWebSocketFrame(alloc(binary)));
    }
}