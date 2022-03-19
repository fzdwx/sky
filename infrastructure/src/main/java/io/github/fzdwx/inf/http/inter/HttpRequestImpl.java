package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Listener;
import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.HttpRequest;
import io.github.fzdwx.inf.msg.WebSocketHandler;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import static io.github.fzdwx.inf.route.inter.RequestMethod.of;
import static io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 19:58
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
    public void upgradeToWebSocket(Listener listener) {
        final var session = SocketSession.create(ctx.channel(), request);
        String subProtocols = null;

        // handshake
        listener.beforeHandshake(session);

        // get subProtocol
        if (session.channel().hasAttr(Netty.SubProtocolAttrKey)) {
            subProtocols = session.channel().attr(Netty.SubProtocolAttrKey).get();
        }

        final var handShaker = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), subProtocols, true)
                .newHandshaker(request);
        if (handShaker == null) {
            sendUnsupportedVersionResponse(session.channel());
        } else {
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

        }
    }

    private static String getWebSocketLocation(final FullHttpRequest req) {
        final String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }
}