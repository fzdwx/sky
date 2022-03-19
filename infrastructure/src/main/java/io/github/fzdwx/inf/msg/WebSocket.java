package io.github.fzdwx.inf.msg;

import io.github.fzdwx.inf.Listener;
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

    static WebSocket create(SocketSession session) {
        return new WebSocketImpl(session);
    }

    WebSocket send(String text);

    WebSocket send(byte[] text);

    WebSocket sendBinary(byte[] binary);

    WebSocket beforeHandshakeHooks(Hooks<Void> h);

    WebSocket openHooks(Hooks<Void> h);

    WebSocket eventHooks(Hooks<Object> h);

    WebSocket binaryHooks(Hooks<ByteBuf> h);

    WebSocket closeHooks(Hooks<Void> h);

    WebSocket errorHooks(Hooks<Throwable> h);

    WebSocket textHooks(Hooks<String> h);

    Listener toLinstener();
}