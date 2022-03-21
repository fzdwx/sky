package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.msg.WebSocket;
import io.github.fzdwx.inf.msg.WebSocketHandler;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.fun.Result;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
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
public class HttpServerRequestImpl implements HttpServerRequest {

    private final ChannelHandlerContext ctx;
    private final Channel channel;
    private final HttpRequest request;
    private final RequestMethod methodType;
    private final HttpVersion version;
    private final HttpHeaders headers;
    private final boolean ssl;

    private NvMap params;

    public HttpServerRequestImpl(final ChannelHandlerContext ctx, final boolean ssl, final HttpRequest request) {
        this.ctx = ctx;
        this.channel = ctx.channel();
        this.request = request;
        this.methodType = of(request);
        this.version = request.protocolVersion();
        this.headers = request.headers();
        this.ssl = ssl;
    }

    @Override
    public HttpVersion version() {
        return version;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public NvMap params() {
        if (params == null) {
            params = Netty.params(uri());
        }

        return params;
    }

    @Override
    public boolean ssl() {
        return this.ssl;
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
        upgradeToWebSocket().then(h);
    }

    @Override
    public Result<WebSocket> upgradeToWebSocket() {
        return (h) -> {
            //region init websocket and convert to linstener
            String subProtocols = null;
            final var session = SocketSession.create(channel);
            final var webSocket = WebSocket.create(session, this);
            //endregion

            h.call(webSocket);

            // handshake
            webSocket.beforeHandshake(session);

            //region parse subProtocol
            if (session.channel().hasAttr(Netty.SubProtocolAttrKey)) {
                subProtocols = session.channel().attr(Netty.SubProtocolAttrKey).get();
            }
            //endregion

            final var handShaker = new WebSocketServerHandshakerFactory(getWebSocketLocation(ssl, request), subProtocols, true)
                    .newHandshaker(request);
            if (handShaker != null) {
                final ChannelPipeline pipeline = ctx.pipeline();
                pipeline.remove(ctx.name());

                // heart beat
                // pipeline.addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));

                // websocket compress
                // pipeline.addLast(new WebSocketServerCompressionHandler());

                // add handler
                pipeline.addLast(new WebSocketHandler(webSocket, session));

                handShaker.handshake(session.channel(), request).addListener(future -> {
                    if (future.isSuccess()) {
                        webSocket.onOpen(session);
                    } else {
                        handShaker.close(session.channel(), new CloseWebSocketFrame());
                    }
                });
            } else {
                sendUnsupportedVersionResponse(session.channel());
            }
        };
    }

    private static String getWebSocketLocation(final boolean ssl, final HttpRequest req) {
        String scheme = ssl ? "wss" : "ws";

        return scheme + "://" + req.headers().get(HttpHeaderNames.HOST) + req.uri();
    }
}