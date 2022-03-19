package io.github.fzdwx.inf.route.msg.inter;

import io.github.fzdwx.inf.route.msg.SocketSession;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static io.github.fzdwx.inf.Netty.alloc;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 20:18
 */
public class SocketSessionImpl implements SocketSession {

    private final Channel channel;
    private final FullHttpRequest request;

    public SocketSessionImpl(final Channel channel, final FullHttpRequest request) {
        this.channel = channel;
        this.request = request;
    }

    @Override
    public Channel channel() {
        return this.channel;
    }

    @Override
    public void send(final String text) {
        this.channel.writeAndFlush(new TextWebSocketFrame(text));
    }

    @Override
    public void send(final byte[] text) {
        this.channel.writeAndFlush(new TextWebSocketFrame(alloc(text)));
    }

    @Override
    public void sendBinary(final byte[] binary) {
        this.channel.writeAndFlush(new BinaryWebSocketFrame(alloc(binary)));
    }
}