package http;

import core.Netty;
import http.inter.HttpServerRequestImpl;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.fun.Result;
import io.github.fzdwx.lambada.http.HttpMethod;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import serializer.JsonSerializer;
import socket.WebSocket;

/**
 * http request.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 19:57
 * @since 0.06
 */
public interface HttpServerRequest {

    static HttpServerRequest create(final ChannelHandlerContext ctx,
                                    final boolean ssl,
                                    final FullHttpRequest request,
                                    final HttpDataFactory httpDataFactory,
                                    final JsonSerializer serializer) {
        return new HttpServerRequestImpl(ctx, ssl, request, httpDataFactory, serializer);
    }

    HttpVersion version();

    HttpHeaders headers();

    void release();

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

    ByteBuf readJson();

    default String readJsonString() {
        return Netty.read(readJson());
    }

    Attribute readBody(String key);

    FileUpload readFile(String key);

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

    Result<WebSocket> upgradeToWebSocket();
}