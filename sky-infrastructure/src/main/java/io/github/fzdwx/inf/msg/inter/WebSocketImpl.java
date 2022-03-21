package io.github.fzdwx.inf.msg.inter;

import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.msg.WebSocket;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;

/**
 * default impl websocket.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/19 16:16
 * @see WebSocket
 * @since 0.06
 */
@Getter
public class WebSocketImpl implements WebSocket {

    private final SocketSession session;
    private final HttpServerRequest httpServerRequest;
    private Hooks<String> textHooks;
    private Hooks<Void> beforeHandshakeHooks;
    private Hooks<Void> openHooks;
    private Hooks<Object> eventHooks;
    private Hooks<ByteBuf> binaryHooks;
    private Hooks<Void> closeHooks;
    private Hooks<Throwable> errorHooks;

    public WebSocketImpl(SocketSession session, final HttpServerRequest httpServerRequest) {
        this.session = session;
        this.httpServerRequest = httpServerRequest;
    }

    @Override
    public Channel channel() {
        return this.session.channel();
    }

    @Override
    public ChannelFuture reject() {
        return session.reject();
    }

    @Override
    public ChannelFuture reject(final String text) {
        return session.reject(text);
    }

    @Override
    public ChannelFuture send(final String text) {
        return session.send(text);
    }

    @Override
    public WebSocket send(final String text, final Hooks<ChannelFuture> h) {
        h.call(send(text));
        return this;
    }

    @Override
    public ChannelFuture send(final byte[] text) {
        return session.send(text);
    }

    @Override
    public WebSocket send(final byte[] text, final Hooks<ChannelFuture> h) {
        h.call(send(text));
        return this;
    }

    @Override
    public ChannelFuture sendBinary(final byte[] binary) {
        return session.sendBinary(binary);
    }

    @Override
    public WebSocket sendBinary(final byte[] binary, final Hooks<ChannelFuture> h) {
        h.call(sendBinary(binary));

        return this;
    }

    public WebSocket registerBeforeHandshake(Hooks<Void> h) {
        this.beforeHandshakeHooks = h;
        return this;
    }

    @Override
    public WebSocket registerOpen(final Hooks<Void> h) {
        this.openHooks = h;
        return this;
    }

    @Override
    public WebSocket registerEvent(final Hooks<Object> h) {
        this.eventHooks = h;
        return this;
    }

    public WebSocket registerText(Hooks<String> h) {
        this.textHooks = h;
        return this;
    }

    @Override
    public WebSocket registerBinary(final Hooks<ByteBuf> h) {
        this.binaryHooks = h;
        return this;
    }

    @Override
    public WebSocket registerClose(final Hooks<Void> h) {
        this.closeHooks = h;
        return this;
    }

    @Override
    public WebSocket registerError(final Hooks<Throwable> h) {
        this.errorHooks = h;
        return this;
    }


    @Override
    public void beforeHandshake(final SocketSession session) throws RuntimeException {
        if (beforeHandshakeHooks != null) {
            beforeHandshakeHooks.call(null);
        }
    }

    @Override
    public void onOpen(final SocketSession session) {
        if (openHooks != null) {
            openHooks.call(null);
        }
    }

    @Override
    public void onclose(final SocketSession session) {
        if (closeHooks != null) {
            closeHooks.call(null);
        }
    }

    @Override
    public void onEvent(final SocketSession session, final Object event) {
        if (eventHooks != null) {
            eventHooks.call(event);
        }
    }

    @Override
    public void onText(final SocketSession session, final String text) {
        if (textHooks != null) {
            textHooks.call(text);
        }
    }

    @Override
    public void onBinary(final SocketSession session, final ByteBuf content) {
        if (binaryHooks != null) {
            binaryHooks.call(content);
        }
    }

    @Override
    public void onError(final SocketSession session, final Throwable cause) {
        if (errorHooks != null) {
            errorHooks.call(cause);
        }
    }
}