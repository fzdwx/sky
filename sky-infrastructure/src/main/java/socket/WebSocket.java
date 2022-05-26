package socket;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import socket.inter.WebSocketImpl;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/19 16:07
 * @since 0.06
 */
public interface WebSocket extends Listener, Socket {

    static WebSocket create(Socket session, final HttpServerRequest httpServerRequest) {
        return new WebSocketImpl(session, httpServerRequest);
    }

    /**
     * @since 0.07
     */
    WebSocket send(String text, Hooks<ChannelFuture> h);

    /**
     * @since 0.07
     */
    WebSocket send(byte[] text, Hooks<ChannelFuture> h);

    ChannelFuture sendBinary(byte[] binary);

    /**
     * @since 0.07
     */
    WebSocket sendBinary(byte[] binary, Hooks<ChannelFuture> h);

    ChannelFuture sendPing(byte[] binary);

    WebSocket sendPing(byte[] binary, Hooks<ChannelFuture> h);

    ChannelFuture sendPong(byte[] binary);

    WebSocket sendPong(byte[] binary, Hooks<ChannelFuture> h);

    /**
     * before handshake.
     */
    WebSocket mountBeforeHandshake(Hooks<Void> h);

    /**
     * on client connect server success.
     */
    WebSocket mountOpen(Hooks<Void> h);

    /**
     * @see io.netty.channel.ChannelInboundHandler#userEventTriggered
     */
    WebSocket mountEvent(Hooks<Object> h);

    /**
     * when client send {@link  TextWebSocketFrame}
     */
    WebSocket mountText(Hooks<String> h);

    /**
     * when client send {@link  BinaryWebSocketFrame}
     */
    WebSocket mountBinary(Hooks<ByteBuf> h);

    /**
     * when client send {@link  PingWebSocketFrame}
     */
    WebSocket mountPing(Hooks<ByteBuf> p);

    /**
     * when client send {@link  PongWebSocketFrame}
     */
    WebSocket mountPong(Hooks<ByteBuf> p);

    /**
     * on client or server close connection while call this method.
     *
     * @apiNote can not send message to client.
     */
    WebSocket mountClose(Hooks<Void> h);

    /**
     * on error while call this method.
     */
    WebSocket mountError(Hooks<Throwable> h);

    @Override
    <T> WebSocket attr(String key, T value);

    @Override
    <T> Socket attr(AttributeKey<T> key, T value);

    @Override
    <T> T attr(String key);

    @Override
    <T> T attr(AttributeKey<T> key);

    @Override
    boolean hasAttr(String key);

    @Override
    boolean hasAttr(AttributeKey<?> key);
}