package core;

import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * netty tool.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 21:44
 * @since 0.06
 */
@Slf4j
public final class Netty {

    public static final AttributeKey<String> SubProtocolAttrKey = AttributeKey.valueOf("subProtocol");
    public static final int DEFAULT_CHUNK_SIZE = 8192;
    public static final ByteBuf empty = Unpooled.EMPTY_BUFFER;

    public static GenericFutureListener<? extends Future<? super Void>> close = ChannelFutureListener.CLOSE;

    public static String read(ByteBuf buf) {
        return new String(readBytes(buf));
    }

    public static byte[] readBytes(ByteBuf buf) {
        final var dest = new byte[buf.readableBytes()];

        buf.readBytes(dest);

        return dest;
    }

    public static ByteBuf wrap(final ByteBufAllocator alloc, final byte[] binary) {
        if (binary == null || binary.length <= 0) {
            return empty;
        }
        final ByteBuf buffer = alloc.buffer(binary.length);
        return buffer.writeBytes(binary);
    }

    public static ByteBuf wrap(final ByteBufAllocator alloc, final String data) {
        return wrap(alloc, data.getBytes());
    }


    public static boolean isWebSocket(HttpHeaders headers) {
        return headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }

    public static boolean isTransferEncodingChunked(HttpHeaders headers) {
        return headers.containsValue(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true);
    }

    public static boolean isContentLengthSet(HttpHeaders headers) {
        return headers.contains(HttpHeaderNames.CONTENT_LENGTH);
    }

    public static void setTransferEncodingChunked(HttpHeaders headers, boolean chunked) {
        if (chunked) {
            headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            headers.remove(HttpHeaderNames.CONTENT_LENGTH);
        } else {
            List<String> encodings = headers.getAll(HttpHeaderNames.TRANSFER_ENCODING);
            if (encodings.isEmpty()) {
                return;
            }
            List<CharSequence> values = new ArrayList<CharSequence>(encodings);
            values.removeIf(HttpHeaderValues.CHUNKED::contentEqualsIgnoreCase);
            if (values.isEmpty()) {
                headers.remove(HttpHeaderNames.TRANSFER_ENCODING);
            } else {
                headers.set(HttpHeaderNames.TRANSFER_ENCODING, values);
            }
        }
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
        return params;
    }

    public static int findPathEndIndex(String uri) {
        int len = uri.length();
        for (int i = 0; i < len; i++) {
            char c = uri.charAt(i);
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return len;
    }

    public static ChannelPromise promise(final Channel channel) {
        return new DefaultChannelPromise(channel);
    }

    public static ChannelFuture future(final Channel channel) {
        return new DefaultChannelPromise(channel);
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    public static void removeHandler(final Channel channel, final String handlerName) {
        if (channel.isActive() && channel.pipeline()
                .context(handlerName) != null) {
            channel.pipeline()
                    .remove(handlerName);
        }
    }

    public static void replaceHandler(Channel channel, String handlerName, ChannelHandler handler) {
        if (channel.isActive() && channel.pipeline()
                .context(handlerName) != null) {
            channel.pipeline()
                    .replace(handlerName, handlerName, handler);

        }
    }

    /**
     * Create a new {@link ChannelInboundHandler} that will invoke
     * {@link BiConsumer#accept} on
     * {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}.
     *
     * @param handler the channel-read callback
     * @return a marking event used when a netty connector handler terminates
     */
    public static ChannelInboundHandler inboundHandler(BiConsumer<? super ChannelHandlerContext, Object> handler) {
        return new Netty.ExtractorHandler(handler);
    }

    public final static class InboundIdleStateHandler extends IdleStateHandler {

        final Runnable onReadIdle;

        public InboundIdleStateHandler(long idleTimeout, Runnable onReadIdle) {
            super(idleTimeout, 0, 0, TimeUnit.MILLISECONDS);
            this.onReadIdle = requireNonNull(onReadIdle, "onReadIdle");
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx,
                                   IdleStateEvent evt) throws Exception {
            if (evt.state() == IdleState.READER_IDLE) {
                onReadIdle.run();
            }
            super.channelIdle(ctx, evt);
        }
    }

    public final static class OutboundIdleStateHandler extends IdleStateHandler {

        final Runnable onWriteIdle;

        public OutboundIdleStateHandler(long idleTimeout, Runnable onWriteIdle) {
            super(0, idleTimeout, 0, TimeUnit.MILLISECONDS);
            this.onWriteIdle = requireNonNull(onWriteIdle, "onWriteIdle");
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx,
                                   IdleStateEvent evt) throws Exception {
            if (evt.state() == IdleState.WRITER_IDLE) {
                onWriteIdle.run();
            }
            super.channelIdle(ctx, evt);
        }
    }

    @ChannelHandler.Sharable
    public static final class ExtractorHandler extends ChannelInboundHandlerAdapter {


        final BiConsumer<? super ChannelHandlerContext, Object> extractor;

        public ExtractorHandler(BiConsumer<? super ChannelHandlerContext, Object> extractor) {
            this.extractor = Objects.requireNonNull(extractor, "extractor");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            extractor.accept(ctx, msg);
        }

    }

}