package socket.inter;

import core.Netty;
import http.HttpServerRequest;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.Getter;
import socket.Socket;
import socket.WebSocket;

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

    private final Socket socket;
    private final HttpServerRequest httpServerRequest;
    private Hooks<String> textHooks;
    private Hooks<Void> beforeHandshakeHooks;
    private Hooks<Void> openHooks;
    private Hooks<Object> eventHooks;
    private Hooks<ByteBuf> binaryHooks;
    private Hooks<Void> closeHooks;
    private Hooks<Throwable> errorHooks;

    public WebSocketImpl(Socket socket, final HttpServerRequest httpServerRequest) {
        this.socket = socket;
        this.httpServerRequest = httpServerRequest;
    }

    @Override
    public Channel channel() {
        return this.socket.channel();
    }

    @Override
    public ChannelFuture reject() {
        return socket.reject();
    }

    @Override
    public ChannelFuture reject(final String text) {
        return socket.reject(text);
    }

    @Override
    public ChannelFuture send(final String text) {
        return send(text.getBytes());
    }

    @Override
    public ChannelFuture send(final byte[] text) {
        return this.channel().writeAndFlush(new TextWebSocketFrame(Netty.wrap(channel().alloc(), text)));
    }

    @Override
    public WebSocket send(final String text, final Hooks<ChannelFuture> h) {
        h.call(send(text));
        return this;
    }

    @Override
    public WebSocket send(final byte[] text, final Hooks<ChannelFuture> h) {
        h.call(send(text));
        return this;
    }

    @Override
    public ChannelFuture sendBinary(final byte[] binary) {
        return channel().writeAndFlush(new BinaryWebSocketFrame(Netty.wrap(channel().alloc(), binary)));
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
    public <T> WebSocket attr(final String key, final T value) {
        this.socket.attr(key, value);
        return this;
    }

    @Override
    public <T> Socket attr(final AttributeKey<T> key, final T value) {
        this.socket.attr(key, value);
        return this;
    }

    @Override
    public <T> T attr(final String key) {
        return this.socket.attr(key);
    }

    @Override
    public <T> T attr(final AttributeKey<T> key) {
        return this.socket.attr(key);
    }

    @Override
    public boolean hasAttr(final String key) {
        return this.socket.hasAttr(key);
    }

    @Override
    public boolean hasAttr(final AttributeKey<?> key) {
        return this.socket.hasAttr(key);
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