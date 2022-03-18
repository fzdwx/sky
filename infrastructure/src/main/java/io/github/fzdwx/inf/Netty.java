package io.github.fzdwx.inf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * netty tool.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 21:44
 */
public final class Netty {

    public static final AttributeKey<String> SubProtocolAttrKey = AttributeKey.valueOf("subProtocol");

    public static GenericFutureListener<? extends Future<? super Void>> close = ChannelFutureListener.CLOSE;

    public static String read(ByteBuf buf) {
        final var dest = new byte[buf.readableBytes()];

        buf.readBytes(dest);

        return new String(dest);
    }

    public static ByteBuf allocInt() {
        return Unpooled.buffer(4);
    }

    public static boolean isWebSocket(HttpHeaders headers) {
        return headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }
}