package socket;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
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
     * on client send text message while call this method.
     */
    WebSocket mountText(Hooks<String> h);

    /**
     * on client send binary message while call this method.
     */
    WebSocket mountBinary(Hooks<ByteBuf> h);

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

    /**
     * @deprecated External calls are not recommended
     */
    @Override
    void beforeHandshake(final Socket session) throws RuntimeException;

    /**
     * @deprecated External calls are not recommended
     */
    @Override
    void onOpen(final Socket session);

    /**
     * @deprecated External calls are not recommended
     */
    @Override
    void onclose(final Socket session);

    /**
     * @deprecated External calls are not recommended
     */
    @Override
    void onEvent(final Socket session, final Object event);

    /**
     * @deprecated External calls are not recommended
     */
    @Override
    void onText(final Socket session, final String text);

    /**
     * @deprecated External calls are not recommended
     */
    @Override
    void onBinary(final Socket session, final ByteBuf content);

    /**
     * @deprecated External calls are not recommended
     */
    @Override
    void onError(final Socket session, final Throwable cause);
}