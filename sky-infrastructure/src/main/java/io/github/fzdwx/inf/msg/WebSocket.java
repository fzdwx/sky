package io.github.fzdwx.inf.msg;

import io.github.fzdwx.inf.Listener;
import io.github.fzdwx.inf.http.core.HttpRequest;
import io.github.fzdwx.inf.msg.inter.WebSocketImpl;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/19 16:07
 * @since 0.06
 */
public interface WebSocket extends Listener {

    static WebSocket create(SocketSession session, final HttpRequest httpRequest) {
        return new WebSocketImpl(session, httpRequest);
    }

    ChannelFuture send(String text);

    /**
     * @since 0.07
     */
    WebSocket send(String text, Hooks<ChannelFuture> h);

    ChannelFuture send(byte[] text);

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
    WebSocket registerBeforeHandshake(Hooks<Void> h);

    /**
     * on client connect server success.
     */
    WebSocket registerOpen(Hooks<Void> h);

    /**
     * @see io.netty.channel.ChannelInboundHandler#userEventTriggered
     */
    WebSocket registerEvent(Hooks<Object> h);

    /**
     * on client send text message while call this method.
     */
    WebSocket registerText(Hooks<String> h);

    /**
     * on client send binary message while call this method.
     */
    WebSocket registerBinary(Hooks<ByteBuf> h);

    /**
     * on client close connection while call this method.
     *
     * @apiNote can not send message to client.
     */
    WebSocket registerClose(Hooks<Void> h);

    /**
     * on error while call this method.
     */
    WebSocket registerError(Hooks<Throwable> h);

    /**
     * @deprecated
     */
    @Override
    void onOpen(final SocketSession session);

    /**
     * @deprecated
     */
    @Override
    void onclose(final SocketSession session);

    /**
     * @deprecated
     */
    @Override
    void onEvent(final SocketSession session, final Object event);

    /**
     * @deprecated
     */
    @Override
    void onText(final SocketSession session, final String text);

    /**
     * @deprecated
     */
    @Override
    void onBinary(final SocketSession session, final ByteBuf content);

    /**
     * @deprecated
     */
    @Override
    void onError(final SocketSession session, final Throwable cause);
}