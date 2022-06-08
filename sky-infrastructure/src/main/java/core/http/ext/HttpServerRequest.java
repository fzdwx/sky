package core.http.ext;

import core.http.Headers;
import core.http.inter.AggHttpServerRequest;
import core.http.inter.HttpServerRequestImpl;
import core.serializer.JsonSerializer;
import io.github.fzdwx.lambada.anno.NonNull;
import io.github.fzdwx.lambada.anno.Nullable;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.http.HttpMethod;
import io.github.fzdwx.lambada.lang.KvMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.FileUpload;
import util.Netty;

import java.net.SocketAddress;
import java.util.Collection;

/**
 * http request.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 19:57
 * @since 0.06
 */
public interface HttpServerRequest {

    /**
     * Release resources and end the life cycle
     */
    void destroy();

    /**
     * this request is multipart?
     */
    boolean multipart();

    /**
     * this request is formUrlEncoder?
     */
    boolean formUrlEncoder();

    /**
     * @see DecoderResult
     */
    DecoderResult decoderResult();

    /**
     * remote address
     */
    SocketAddress remoteAddress();

    /**
     * http version
     *
     * @return {@link HttpVersion }
     * @see HttpVersion#HTTP_1_1
     * @see HttpVersion#HTTP_1_0
     */
    HttpVersion version();

    /**
     * http headers
     *
     * @see io.netty.handler.codec.http.HttpHeaders
     */
    Headers headers();

    /**
     * url params
     * <p>
     * e.g <code>http://www.baidu.com?a=1&b=2</code> => a=1,b=2
     */
    KvMap params();

    /**
     * ues ssl?
     *
     * @return boolean
     */
    boolean ssl();

    /**
     * json serializer.
     */
    JsonSerializer serializer();

    /**
     * get file from form.
     */
    default HttpServerRequest readFile(Hooks<FileUpload> hooks, String key) {
        hooks.call(readFile(key));
        return this;
    }

    /**
     * get all files from form.
     */
    default HttpServerRequest readFiles(Hooks<Collection<FileUpload>> hooks) {
        hooks.call(readFiles());

        return this;
    }

    /**
     * read body,maybe null.
     */
    @Nullable
    ByteBuf body();

    /**
     * read body as string,never null.
     */
    @NonNull
    default String bodyToString() {
        return Netty.read(body());
    }

    /**
     * get all attributes from form.
     */
    @Nullable
    KvMap formAttributes();

    /**
     * get file from form.
     */
    @Nullable
    FileUpload readFile(String key);

    /**
     * get all files from form.
     */
    @Nullable
    Collection<FileUpload> readFiles();

    /**
     * request uri
     *
     * @return {@link String }
     */
    String uri();

    /**
     * url path like /a/b/c
     *
     * @return {@link String }
     */
    String path();

    /**
     * url query like a=1&b=2
     */
    String query();

    /**
     * request type
     *
     * @return {@link HttpMethod }
     */
    HttpMethod methodType();

    /**
     * accept websocket.
     */
    void upgradeToWebSocket(Hooks<WebSocket> h);

    /**
     * upgrade to websocket?
     */
    boolean isWebsocket();

    /**
     * source request.
     */
    HttpRequest nettyRequest();

    /**
     * http content type.
     */
    String contentType();

    static HttpServerRequest create(final ChannelHandlerContext ctx, final boolean ssl, AggHttpServerRequest msg, final JsonSerializer serializer) {
        return new HttpServerRequestImpl(ctx, ssl, msg, serializer);
    }
}