package core.http.inter;

import cn.hutool.core.util.StrUtil;
import core.http.Headers;
import core.http.ext.HttpServerRequest;
import core.serializer.JsonSerializer;
import core.socket.Socket;
import core.socket.WebSocket;
import core.socket.WebSocketHandler;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.http.HttpMethod;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
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

import java.net.SocketAddress;

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
    private InterfaceHttpPostRequestDecoder bodyDecoder;
    private NvMap params;
    private boolean websocketFlag;

    public HttpServerRequestImpl(final ChannelHandlerContext ctx,
                                 final boolean ssl,
                                 final AggHttpServerRequest msg,
                                 final JsonSerializer serializer) {
        this.ctx = ctx;
        this.channel = ctx.channel();
        this.nettyRequest = msg;
        this.headers = msg.header();
        this.ssl = ssl;
        this.serializer = serializer;
        this.bodyDecoder = msg.bodyDecoder();
        this.multipartFlag = msg.multipart();
        this.formUrlEncoderFlag = msg.formUrlEncoder();
    }

    @Override
    public void destroy() {
        this.nettyRequest.release();
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
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public HttpVersion version() {
        return nettyRequest.protocolVersion();
    }

    @Override
    public Headers header() {
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
    public JsonSerializer serializer() {
        return this.serializer;
    }

    @Override
    public ByteBuf readJson() {
        return this.nettyRequest.content();
    }

    @Override
    public Attribute readBody(final String key) {
        if (bodyDecoder == null) return null;

        return (Attribute) bodyDecoder.getBodyHttpData(key);
    }

    @Override
    public FileUpload readFile(String key) {
        if (bodyDecoder == null) return null;

        return (FileUpload) bodyDecoder.getBodyHttpData(key);
    }

    @Override
    public Seq<FileUpload> readFiles() {
        if (bodyDecoder == null) return null;

        return Seq.of(bodyDecoder.getBodyHttpDatas())
                .filter(d -> d.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload))
                .typeOf(FileUpload.class);
    }

    @Override
    public String uri() {
        return nettyRequest.uri();
    }

    @Override
    public String path() {
        if (this.path == null) {
            final int endIndex = Netty.findPathEndIndex(uri());
            this.path = StrUtil.sub(uri(), 0, endIndex);
        }
        return this.path;
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

            // add handler
            pipeline.addLast(new WebSocketHandler(webSocket, session));

            handShaker.handshake(session.channel(), nettyRequest).addListener(future -> {
                if (future.isSuccess()) {
                    webSocket.onOpen(session);
                } else {
                    handShaker.close(session.channel(), new CloseWebSocketFrame());
                }
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

    private static String getWebSocketLocation(final WebSocket ws, final HttpRequest req) {
        return ws.scheme().name() + "://" + req.headers().get(HttpHeaderNames.HOST) + req.uri();
    }
}