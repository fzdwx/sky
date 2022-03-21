package io.github.fzdwx.inf;

import io.github.fzdwx.inf.http.HttpServ;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;
import java.util.Map;

/**
 * netty tool.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 21:44
 * @since 0.06
 */
public final class Netty {

    public static final AttributeKey<String> SubProtocolAttrKey = AttributeKey.valueOf("subProtocol");
    public static final int DEFAULT_CHUNK_SIZE = 1024 * 1024 * 4;
    public static GenericFutureListener<? extends Future<? super Void>> close = ChannelFutureListener.CLOSE;
    public static ByteBuf empty = Unpooled.EMPTY_BUFFER;
    public static Hooks<ChannelFuture> pass = (f) -> { };

    public static String read(ByteBuf buf) {
        final var dest = new byte[buf.readableBytes()];

        buf.readBytes(dest);

        return new String(dest);
    }

    public static ByteBuf allocInt() {
        return Unpooled.buffer(4);
    }

    public static ByteBuf alloc(final byte[] binary) {
        return Unpooled.buffer(binary.length).writeBytes(binary);
    }

    public static boolean isWebSocket(HttpHeaders headers) {
        return headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }

    /**
     * create a new http server.
     *
     * @param port   the http server listening port
     * @param router the http router
     * @return {@link HttpServ }
     * @see Router
     */
    public static HttpServ HTTP(int port, Router router) {
        return new HttpServ(port, router);
    }

    public static NvMap params(final String uri) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        final var parameters = queryStringDecoder.parameters();

        NvMap params = NvMap.create();
        if (!parameters.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                params.add(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }
}