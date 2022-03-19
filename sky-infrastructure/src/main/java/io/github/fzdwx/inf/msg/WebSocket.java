package io.github.fzdwx.inf.msg;

import io.github.fzdwx.inf.Listener;
import io.github.fzdwx.inf.http.core.HttpRequest;
import io.github.fzdwx.inf.msg.inter.WebSocketImpl;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/19 16:07
 * @since 0.06
 */
public interface WebSocket {

    static WebSocket create(SocketSession session, final HttpRequest httpRequest) {
        return new WebSocketImpl(session,httpRequest);
    }

    WebSocket send(String text);

    WebSocket send(byte[] text);

    WebSocket sendBinary(byte[] binary);

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

    Listener toListener();
}