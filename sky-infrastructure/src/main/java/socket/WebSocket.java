package socket;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
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

    /**
     * @deprecated
     */
    @Override
    void beforeHandshake(final Socket session) throws RuntimeException;

    /**
     * @deprecated
     */
    @Override
    void onOpen(final Socket session);

    /**
     * @deprecated
     */
    @Override
    void onclose(final Socket session);

    /**
     * @deprecated
     */
    @Override
    void onEvent(final Socket session, final Object event);

    /**
     * @deprecated
     */
    @Override
    void onText(final Socket session, final String text);

    /**
     * @deprecated
     */
    @Override
    void onBinary(final Socket session, final ByteBuf content);

    /**
     * @deprecated
     */
    @Override
    void onError(final Socket session, final Throwable cause);
}