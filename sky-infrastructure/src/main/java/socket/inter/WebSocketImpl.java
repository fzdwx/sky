package socket.inter;

import http.HttpServerRequest;
import socket.WebSocket;
import socket.Socket;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;

/**
 * default impl websocket.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/19 16:16
 * @see WebSocket
 * @since 0.06
 */
@Getter
public class WebSocketImpl implements WebSocket {

    private final Socket session;
    private final HttpServerRequest httpServerRequest;
    private Hooks<String> textHooks;
    private Hooks<Void> beforeHandshakeHooks;
    private Hooks<Void> openHooks;
    private Hooks<Object> eventHooks;
    private Hooks<ByteBuf> binaryHooks;
    private Hooks<Void> closeHooks;
    private Hooks<Throwable> errorHooks;

    public WebSocketImpl(Socket session, final HttpServerRequest httpServerRequest) {
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

    public WebSocket mountBeforeHandshake(Hooks<Void> h) {
        this.beforeHandshakeHooks = h;
        return this;
    }

    @Override
    public WebSocket mountOpen(final Hooks<Void> h) {
        this.openHooks = h;
        return this;
    }

    @Override
    public WebSocket mountEvent(final Hooks<Object> h) {
        this.eventHooks = h;
        return this;
    }

    public WebSocket mountText(Hooks<String> h) {
        this.textHooks = h;
        return this;
    }

    @Override
    public WebSocket mountBinary(final Hooks<ByteBuf> h) {
        this.binaryHooks = h;
        return this;
    }

    @Override
    public WebSocket mountClose(final Hooks<Void> h) {
        this.closeHooks = h;
        return this;
    }

    @Override
    public WebSocket mountError(final Hooks<Throwable> h) {
        this.errorHooks = h;
        return this;
    }


    @Override
    public void beforeHandshake(final Socket session) throws RuntimeException {
        if (beforeHandshakeHooks != null) {
            beforeHandshakeHooks.call(null);
        }
    }

    @Override
    public void onOpen(final Socket session) {
        if (openHooks != null) {
            openHooks.call(null);
        }
    }

    @Override
    public void onclose(final Socket session) {
        if (closeHooks != null) {
            closeHooks.call(null);
        }
    }

    @Override
    public void onEvent(final Socket session, final Object event) {
        if (eventHooks != null) {
            eventHooks.call(event);
        }
    }

    @Override
    public void onText(final Socket session, final String text) {
        if (textHooks != null) {
            textHooks.call(text);
        }
    }

    @Override
    public void onBinary(final Socket session, final ByteBuf content) {
        if (binaryHooks != null) {
            binaryHooks.call(content);
        }
    }

    @Override
    public void onError(final Socket session, final Throwable cause) {
        if (errorHooks != null) {
            errorHooks.call(cause);
        }
    }
}