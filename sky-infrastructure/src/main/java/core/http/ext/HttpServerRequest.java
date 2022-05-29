package core.http.ext;

import core.http.Headers;
import core.http.inter.AggHttpServerRequest;
import core.http.inter.HttpServerRequestImpl;
import core.serializer.JsonSerializer;
import core.socket.WebSocket;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.anno.NonNull;
import io.github.fzdwx.lambada.anno.Nullable;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.http.HttpMethod;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import util.Netty;

import java.net.SocketAddress;

/**
 * http request.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 19:57
 * @since 0.06
 */
public interface HttpServerRequest {

    void destroy();

    boolean multipart();

    boolean formUrlEncoder();

    SocketAddress remoteAddress();

    HttpVersion version();

    Headers header();

    NvMap params();

    boolean ssl();

    JsonSerializer serializer();

    default HttpServerRequest readFile(Hooks<FileUpload> hooks, String key) {
        hooks.call(readFile(key));
        return this;
    }

    default HttpServerRequest readFiles(Hooks<Seq<FileUpload>> hooks) {
        hooks.call(readFiles());

        return this;
    }

    @Nullable
    ByteBuf readJson();

    @NonNull
    default String readJsonString() {
        return Netty.read(readJson());
    }

    @Nullable
    Attribute readBody(String key);

    @Nullable
    FileUpload readFile(String key);

    @Nullable
    Seq<FileUpload> readFiles();

    /**
     * request uri
     *
     * @return {@link String }
     */
    String uri();

    String path();

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

    boolean isWebsocket();

    HttpRequest nettyRequest();

    String contentType();

    static HttpServerRequest create(final ChannelHandlerContext ctx, final boolean ssl, AggHttpServerRequest msg, final JsonSerializer serializer) {
        return new HttpServerRequestImpl(ctx, ssl, msg, serializer);
    }
}