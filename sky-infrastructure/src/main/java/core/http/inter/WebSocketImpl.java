package core.http.inter;

import core.http.ext.HttpServerRequest;
import core.http.ext.WebSocket;
import core.socket.Socket;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketScheme;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.Getter;
import util.Netty;

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
    private Hooks<Void> beforeHandshakeHooks;
    private Hooks<Void> openHooks;
    private Hooks<Object> eventHooks;
    private Hooks<String> textHooks;
    private Hooks<byte[]> binaryHooks;
    private Hooks<byte[]> pingHooks;
    private Hooks<byte[]> pongHooks;
    private Hooks<Void> closeHooks;
    private Hooks<Throwable> errorHooks;
    private WebSocketServerCompressionHandler compressionHandler;
    private IdleStateHandler idleStateHandler;
    private WebSocketScheme scheme;
    private WebSocketFrameAggregator webSocketFrameAggregator = new WebSocketFrameAggregator(Integer.MAX_VALUE);

    public WebSocketImpl(Socket socket, final HttpServerRequest httpServerRequest) {
        this.socket = socket;
        this.httpServerRequest = httpServerRequest;
        this.scheme = httpServerRequest.ssl() ? WebSocketScheme.WSS : WebSocketScheme.WS;
    }

    @Override
    public Channel channel() {
        return this.socket.channel();
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
    public WebSocketScheme scheme() {
        return scheme;
    }

    @Override
    public ChannelFuture reject() {
        return reject(WebSocketCloseStatus.NORMAL_CLOSURE);
    }

    @Override
    public <T> WebSocket attr(final String key, final T value) {
        this.socket.attr(key, value);
        return this;
    }

    @Override
    public <T> WebSocket attr(final AttributeKey<T> key, final T value) {
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
    public ChannelFuture reject(final WebSocketCloseStatus status) {
        return socket.channel().writeAndFlush(new CloseWebSocketFrame(status)).addListener(Netty.close);
    }

    @Override
    public WebSocket enableIdleState(final IdleStateHandler handler) {
        this.idleStateHandler = handler;
        return this;
    }

    @Override
    public IdleStateHandler idleStateHandler() {
        return this.idleStateHandler;
    }

    @Override
    public WebSocket enableCompression(final WebSocketServerCompressionHandler handler) {
        this.compressionHandler = handler;
        return this;
    }

    @Override
    public WebSocketServerCompressionHandler compressionHandler() {
        return this.compressionHandler;
    }

    @Override
    public WebSocket webSocketFrameAggregator(final WebSocketFrameAggregator webSocketFrameAggregator) {
        this.webSocketFrameAggregator = webSocketFrameAggregator;
        return this;
    }

    @Override
    public WebSocketFrameAggregator webSocketFrameAggregator() {
        return this.webSocketFrameAggregator;
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

    @Override
    public ChannelFuture sendPing(final byte[] binary) {
        final Channel channel = channel();
        return channel.writeAndFlush(new PingWebSocketFrame(Netty.wrap(channel.alloc(), binary)));
    }

    @Override
    public WebSocket sendPing(final byte[] binary, final Hooks<ChannelFuture> h) {
        h.call(sendPing(binary));
        return this;
    }

    @Override
    public ChannelFuture sendPong(final byte[] binary) {
        final Channel channel = channel();
        return channel.writeAndFlush(new PongWebSocketFrame(Netty.wrap(channel.alloc(), binary)));
    }

    @Override
    public WebSocket sendPong(final byte[] binary, final Hooks<ChannelFuture> h) {
        h.call(sendPong(binary));
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
    public WebSocket mountBinary(final Hooks<byte[]> h) {
        this.binaryHooks = h;
        return this;
    }

    @Override
    public WebSocket mountPing(final Hooks<byte[]> p) {
        this.pingHooks = p;
        return this;
    }

    @Override
    public WebSocket mountPong(final Hooks<byte[]> p) {
        this.pongHooks = p;
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
    public void onBinary(final Socket session, final byte[] content) {
        if (binaryHooks != null) {
            binaryHooks.call(content);
        }
    }

    @Override
    public void onPing(final byte[] ping) {
        if (pingHooks != null) {
            pingHooks.call(ping);
        }
    }

    @Override
    public void onPong(final byte[] pong) {
        if (pongHooks != null) {
            pongHooks.call(pong);
        }
    }

    @Override
    public void onError(final Socket session, final Throwable cause) {
        if (errorHooks != null) {
            errorHooks.call(cause);
        }
    }
}