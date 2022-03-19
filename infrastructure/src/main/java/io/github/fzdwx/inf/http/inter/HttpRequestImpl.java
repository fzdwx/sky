package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.HttpRequest;
import io.github.fzdwx.inf.msg.WebSocket;
import io.github.fzdwx.inf.msg.WebSocketHandler;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import static io.github.fzdwx.inf.route.inter.RequestMethod.of;
import static io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse;

/**
 * http request default implement
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 19:58
 * @since 0.06
 */
public class HttpRequestImpl implements HttpRequest {

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final RequestMethod methodType;

    public HttpRequestImpl(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        this.ctx = ctx;
        this.request = request;
        this.methodType = of(request);
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public RequestMethod methodType() {
        return this.methodType;
    }

    @Override
    public void upgradeToWebSocket(Hooks<WebSocket> h) {
        //region init websocket and convert to linstener
        String subProtocols = null;
        final var session = SocketSession.create(ctx.channel());
        final var webSocket = WebSocket.create(session, this);
        h.call(webSocket);
        final var listener = webSocket.toLinstener();
        //endregion

        // handshake
        listener.beforeHandshake(session);

        //region parse subProtocol
        if (session.channel().hasAttr(Netty.SubProtocolAttrKey)) {
            subProtocols = session.channel().attr(Netty.SubProtocolAttrKey).get();
        }
        //endregion

        final var handShaker = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), subProtocols, true)
                .newHandshaker(request);
        if (handShaker != null) {
            final ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(ctx.name());

            // heart beat
            // pipeline.addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));

            // websocket compress
            // pipeline.addLast(new WebSocketServerCompressionHandler());

            // add handler
            pipeline.addLast(new WebSocketHandler(listener, session));

            handShaker.handshake(session.channel(), request).addListener(future -> {
                if (future.isSuccess()) {
                    listener.onOpen(session);
                } else {
                    handShaker.close(session.channel(), new CloseWebSocketFrame());
                }
            });
        } else {
            sendUnsupportedVersionResponse(session.channel());
        }
    }

    private static String getWebSocketLocation(final FullHttpRequest req) {
        final String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }
}