package http.inter;

import core.Netty;
import http.HttpServerRequest;
import socket.SocketSession;
import socket.WebSocket;
import socket.WebSocketHandler;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.fun.Result;
import io.github.fzdwx.lambada.lang.HttpMethod;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

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
    private final HttpMethod methodType;
    private final HttpVersion version;
    private final HttpHeaders headers;
    private final boolean ssl;
    private final HttpDataFactory httpDataFactory;
    private HttpPostMultipartRequestDecoder bodyDecoder;
    private boolean readBody;
    private NvMap params;
    private final String uri;

    public HttpServerRequestImpl(final ChannelHandlerContext ctx, final boolean ssl,
                                 final HttpRequest request,
                                 final HttpDataFactory httpDataFactory) {
        this.ctx = ctx;
        this.channel = ctx.channel();
        this.request = request;
        this.methodType = HttpMethod.of(request.method().name());
        this.version = request.protocolVersion();
        this.headers = request.headers();
        this.ssl = ssl;
        this.uri = request.uri();
        this.httpDataFactory = httpDataFactory;
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
    public void release() {
        if (readBody) {
            bodyDecoder.destroy();
        }
    }

    @Override
    public NvMap params() {
        if (params == null) {
            params = Netty.params(uri);
        }

        return params;
    }

    @Override
    public boolean ssl() {
        return this.ssl;
    }

    @Override
    public ByteBuf readJson() {
        return ((FullHttpRequest) this.request).content();
    }

    @Override
    public Attribute readBody(final String key) {
        initBody();

        return (Attribute) bodyDecoder.getBodyHttpData(key);
    }

    @Override
    public FileUpload readFile(String key) {
        initBody();

        return (FileUpload) bodyDecoder.getBodyHttpData(key);
    }

    @Override
    public Seq<FileUpload> readFiles() {
        initBody();

        return Seq.of(bodyDecoder.getBodyHttpDatas())
                .filter(d -> d.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload))
                .typeOf(FileUpload.class);
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public HttpMethod methodType() {
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

            // mount hooks
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

    private void initBody() {
        if (!readBody) {
            bodyDecoder = new HttpPostMultipartRequestDecoder(httpDataFactory, request);
            readBody = true;
        }
    }
}