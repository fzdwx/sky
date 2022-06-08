package core.http.ext;

import core.http.inter.WebSocketImpl;
import core.socket.Listener;
import core.socket.Socket;
import io.github.fzdwx.lambada.anno.Nullable;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketScheme;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import util.Netty;

import java.util.concurrent.TimeUnit;

/**
 * websocket channel.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/19 16:07
 * @since 0.06
 */
public interface WebSocket extends Listener, Socket {

    static WebSocket create(Socket socket, final HttpServerRequest httpServerRequest) {
        return new WebSocketImpl(socket, httpServerRequest);
    }

    /**
     * ret scheme
     */
    WebSocketScheme scheme();

    /**
     * close websocket connection
     *
     * @return {@link ChannelFuture }
     */
    @Override
    ChannelFuture reject();

    @Override
    <T> WebSocket attr(String key, T value);

    @Override
    <T> WebSocket attr(AttributeKey<T> key, T value);

    @Override
    <T> T attr(String key);

    @Override
    <T> T attr(AttributeKey<T> key);

    @Override
    boolean hasAttr(String key);

    @Override
    boolean hasAttr(AttributeKey<?> key);

    /**
     * close websocket connection and customer close status.
     *
     * @param status {@link WebSocketCloseStatus}
     * @return {@link ChannelFuture }
     */
    ChannelFuture reject(WebSocketCloseStatus status);

    /**
     * set subProtocols
     *
     * @param subProtocols CSV of supported protocols. Null if sub protocols not supported.
     * @return {@link WebSocket }
     */
    default WebSocket subProtocols(String subProtocols) {
        return attr(Netty.SubProtocolAttrKey, subProtocols);
    }

    /**
     * get subProtocols.
     *
     * @return {@link String }
     */
    default String subProtocols() {
        return attr(Netty.SubProtocolAttrKey);
    }

    /**
     * enable {@link IdleStateHandler},default read idle 10s,write idle 10s.
     *
     * @return {@link WebSocket }
     */
    default WebSocket enableIdleState() {
        return enableIdleState(new IdleStateHandler(10, 10, 0, TimeUnit.SECONDS));
    }

    /**
     * enable IdleState
     *
     * @return {@link WebSocket }
     */
    WebSocket enableIdleState(IdleStateHandler handler);

    /**
     * get {@link IdleStateHandler}
     */
    IdleStateHandler idleStateHandler();

    /**
     * enable compression,off by default.
     *
     * @see WebSocketServerCompressionHandler
     */
    default WebSocket enableCompression() {
        return enableCompression(new WebSocketServerCompressionHandler());
    }

    /**
     * enable compression
     *
     * @see WebSocketServerCompressionHandler
     */
    WebSocket enableCompression(WebSocketServerCompressionHandler handler);

    /**
     * get {@link WebSocketServerCompressionHandler}.
     */
    WebSocketServerCompressionHandler compressionHandler();

    /**
     * customer webSocketFrameAggregator
     *
     * @param webSocketFrameAggregator bodyAggregator
     * @return {@link WebSocket }
     * @apiNote maxContentLength is {@link Integer#MAX_VALUE}
     */
    WebSocket webSocketFrameAggregator(@Nullable WebSocketFrameAggregator webSocketFrameAggregator);

    /**
     * get webSocketFrameAggregator
     */
    @Nullable
    WebSocketFrameAggregator webSocketFrameAggregator();

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
}