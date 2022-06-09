package core.http.inter;

import core.http.Headers;
import core.http.ext.HttpServerRequest;
import core.http.ext.WebSocket;
import core.http.handler.BodyHandler;
import core.http.handler.WebSocketHandler;
import core.serializer.JsonSerializer;
import core.socket.Socket;
import io.github.fzdwx.lambada.Collections;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.http.HttpMethod;
import io.github.fzdwx.lambada.lang.KvMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import util.Netty;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;

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
    private final FullHttpRequest nettyRequest;
    private final JsonSerializer serializer;
    private final boolean ssl;
    private final Headers headers;
    private final boolean multipartFlag;
    private final boolean formUrlEncoderFlag;
    private HttpMethod methodType;
    private String path;
    private String query;
    private InterfaceHttpPostRequestDecoder bodyDecoder;
    private KvMap params;
    private KvMap formAttributes;
    private Map<String, FileUpload> uploadFiles;
    private boolean websocketFlag;

    public HttpServerRequestImpl(final ChannelHandlerContext ctx,
                                 final boolean ssl,
                                 final AggHttpServerRequest msg,
                                 final JsonSerializer serializer) {
        this.ctx = ctx;
        this.channel = ctx.channel();
        this.nettyRequest = msg;
        this.headers = msg.headers();
        this.ssl = ssl;
        this.serializer = serializer;
        this.bodyDecoder = msg.bodyDecoder();
        this.multipartFlag = msg.multipart();
        this.formUrlEncoderFlag = msg.formUrlEncoder();
    }

    @Override
    public void destroy() {
        if (this.nettyRequest.refCnt() > 0) {
            this.nettyRequest.release(this.nettyRequest.refCnt());
        }
        if (this.bodyDecoder != null) {
            this.bodyDecoder.destroy();
            this.bodyDecoder = null;
        }
    }

    @Override
    public boolean multipart() {
        return multipartFlag;
    }

    @Override
    public boolean formUrlEncoder() {
        return formUrlEncoderFlag;
    }

    @Override
    public DecoderResult decoderResult() {
        return nettyRequest.decoderResult();
    }

    @Override
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public HttpVersion version() {
        return nettyRequest.protocolVersion();
    }

    @Override
    public Headers headers() {
        return headers;
    }

    @Override
    public KvMap params() {
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
    public JsonSerializer serializer() {
        return this.serializer;
    }

    @Override
    public ByteBuf body() {
        return this.nettyRequest.content();
    }

    @Override
    public KvMap formAttributes() {
        initFormAttributes();
        return formAttributes;
    }

    @Override
    public FileUpload readFile(String key) {
        initFormAttributes();

        return uploadFiles.get(key);
    }

    @Override
    public Collection<FileUpload> readFiles() {
        initFormAttributes();
        return uploadFiles.values();
    }

    @Override
    public String uri() {
        return nettyRequest.uri();
    }

    @Override
    public String path() {
        if (this.path == null) {
            this.path = Netty.path(uri());
        }
        return this.path;
    }

    @Override
    public String query() {
        if (this.query == null) {
            this.query = Netty.query(uri());
        }
        return this.query;
    }

    @Override
    public HttpMethod methodType() {
        if (this.methodType == null) {
            this.methodType = HttpMethod.of(nettyRequest.method().name());
        }
        return this.methodType;
    }

    @Override
    public void upgradeToWebSocket(Hooks<WebSocket> h) {
        this.websocketFlag = true;
        //region init websocket and convert to linstener
        String subProtocols = null;
        final Socket session = Socket.create(channel);
        final WebSocket webSocket = WebSocket.create(session, this);
        //endregion

        // mount hooks
        h.call(webSocket);

        // handshake
        webSocket.beforeHandshake(session);
        if (!webSocket.channel().isActive() || !webSocket.channel().isOpen()) {
            return;
        }

        //region parse subProtocol
        if (Lang.isNotBlank(webSocket.subProtocols())) {
            subProtocols = webSocket.subProtocols();
        }
        //endregion

        final WebSocketServerHandshaker handShaker =
                new WebSocketServerHandshakerFactory(getWebSocketLocation(webSocket, nettyRequest), subProtocols, true).newHandshaker(nettyRequest);

        if (handShaker != null) {
            final ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(ctx.name());

            // heart beat
            if (webSocket.idleStateHandler() != null) {
                pipeline.addLast(webSocket.idleStateHandler());
            }

            // websocket compress
            if (webSocket.compressionHandler() != null) {
                pipeline.addLast(webSocket.compressionHandler());
            }

            // websocket frame body aggregator
            if (webSocket.webSocketFrameAggregator() != null) {
                pipeline.addLast(webSocket.webSocketFrameAggregator());
            }

            // add handler
            pipeline.addLast(new WebSocketHandler(webSocket, session));

            handShaker.handshake(session.channel(), nettyRequest).addListener(future -> {
                if (future.isSuccess()) {
                    webSocket.onOpen(session);
                } else {
                    handShaker.close(session.channel(), new CloseWebSocketFrame());
                }

                // remove Http request body aggregator
                pipeline.remove(BodyHandler.class);
            });
        } else {
            sendUnsupportedVersionResponse(session.channel());
        }
    }

    @Override
    public boolean isWebsocket() {
        return websocketFlag;
    }

    @Override
    public HttpRequest nettyRequest() {
        return this.nettyRequest;
    }

    @Override
    public String contentType() {
        return nettyRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public String toString() {
        return "{\"method\":\"" + methodType + "\", \"uri\":\"" + uri() + "\",\"remote\":\"" + remoteAddress() + "\", \"version\":\"" + version() + "\"}";
        // return "Http Request{" +
        //         "uri='" + uri + "\n" +
        //         ", method=" + methodType +
        //         ", ssl=" + ssl +
        //         ", httpVersion=" + this.request.protocolVersion() +
        //         ", headers=" + this.headers +
        //         '}';
    }

    private void initFormAttributes() {
        if (this.formAttributes == null) {
            this.formAttributes = KvMap.create();
        }
        if (this.uploadFiles == null) {
            this.uploadFiles = Collections.map();
        }
        if (this.bodyDecoder == null) {
            return;
        }

        while (this.bodyDecoder.hasNext()) {
            final InterfaceHttpData next = this.bodyDecoder.next();
            if (next instanceof Attribute) {
                final Attribute attribute = (Attribute) next;
                try {
                    formAttributes.put(next.getName(), attribute.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    attribute.release();
                }
            }

            if (next instanceof FileUpload) {
                final FileUpload fileUpload = (FileUpload) next;
                uploadFiles.put(fileUpload.getName(), fileUpload);
            }
        }

    }

    private static String getWebSocketLocation(final WebSocket ws, final HttpRequest req) {
        return ws.scheme().name() + "://" + req.headers().get(HttpHeaderNames.HOST) + req.uri();
    }
}