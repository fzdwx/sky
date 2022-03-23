package io.github.fzdwx.inf;

import io.github.fzdwx.inf.core.ChannelOperationsId;
import io.github.fzdwx.inf.core.Connection;
import io.github.fzdwx.inf.core.ConnectionObserver;
import io.github.fzdwx.inf.core.SingleChunkedInput;
import io.github.fzdwx.inf.http.HttpServ;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.lambada.Coll;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.nio.NioEventLoop;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("$CONNECTION");
    public static final AttributeKey<Boolean> PERSISTENT_CHANNEL = AttributeKey.valueOf("$PERSISTENT_CHANNEL");

    public final static String readIdleHandlerName = "fzdwx.customer.readIdleHandler";
    public final static String writeIdleHandlerName = "fzdwx.customer.writeIdleHandler";
    public final static String h2MultiplexHandlerName = "fzdwx.customer.h2MultiplexHandler";
    public final static String compressionHandlerName = "fzdwx.customer.compressionHandler";
    public final static String chunkedWriter = "fzdwx.customer.chunkedWriter";

    public static final int DEFAULT_CHUNK_SIZE = 8192;
    public static final ByteBuf empty = Unpooled.EMPTY_BUFFER;

    public final static ConnectionObserver.State CONNECTED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[connected]";
        }
    };
    public final static ConnectionObserver.State ACQUIRED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[acquired]";
        }
    };
    public final static ConnectionObserver.State CONFIGURED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[configured]";
        }
    };
    public final static ConnectionObserver.State RELEASED = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[released]";
        }
    };
    public final static ConnectionObserver.State DISCONNECTING = new ConnectionObserver.State() {
        @Override
        public String toString() {
            return "[disconnecting]";
        }
    };

    public static final Predicate<Object> PREDICATE_FLUSH = o -> false;
    public static final Predicate<ByteBuf> PREDICATE_BB_FLUSH = b -> false;
    public static final Hooks<ChannelFuture> pass = (f) -> { };
    public static final ConnectionObserver NOOP_LISTENER = (connection, newState) -> { };
    public static final Consumer<? super FileChannel> fileCloser = fc -> {
        try {
            fc.close();
        } catch (Throwable e) {
            if (log.isTraceEnabled()) {
                log.trace("", e);
            }
        }
    };
    static final char CHANNEL_ID_PREFIX = '[';
    static final String CHANNEL_ID_SUFFIX_1 = "] ";
    static final char CHANNEL_ID_SUFFIX_2 = ' ';
    static final String ORIGINAL_CHANNEL_ID_PREFIX = "[id: 0x";
    static final int ORIGINAL_CHANNEL_ID_PREFIX_LENGTH = ORIGINAL_CHANNEL_ID_PREFIX.length();
    static final char TRACE_ID_PREFIX = '(';
    static final boolean LOG_CHANNEL_INFO =
            Boolean.parseBoolean(System.getProperty("reactor.netty.logChannelInfo", "true"));
    public static GenericFutureListener<? extends Future<? super Void>> close = ChannelFutureListener.CLOSE;

    public static String read(ByteBuf buf) {
        final var dest = new byte[buf.readableBytes()];

        buf.readBytes(dest);

        return new String(dest);
    }

    public static ByteBuf allocInt() {
        return Unpooled.buffer(4);
    }

    public static ByteBuf wrap(final byte[] binary) {
        if (binary == null || binary.length <= 0) {
            return empty;
        }
        return Unpooled.wrappedBuffer(binary);
    }

    public static ChunkedInput<ByteBuf> chunked(byte[] data) {
        return SingleChunkedInput.of(data, DEFAULT_CHUNK_SIZE);
    }

    public static ChunkedInput<ByteBuf> chunked(byte[] data, int chunkSize) {
        return SingleChunkedInput.of(data, chunkSize);
    }

    public static void main(String[] args) {
        List<Integer> l1 = Coll.list(1, 2, 3, 4, 5);

        List<Integer> l2 = Coll.list(1, 2, 3, 4, 5, 666, 777);

        System.out.println(Coll.disjunction(l1, l2));
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
     * true: don't use zore copy
     */
    public static boolean mustChunkFileTransfer(final Connection c, final Path file) {
        // if channel multiplexing a parent channel as an http2 stream
        if (c.channel().parent() != null && c.channel().parent().pipeline().get(Netty.h2MultiplexHandlerName) != null) {
            return true;
        }

        ChannelPipeline p = c.channel().pipeline();
        return p.get(SslHandler.class) != null ||
               p.get(Netty.compressionHandlerName) != null ||
               (!(c.channel().eventLoop() instanceof NioEventLoop) &&
                !"file".equals(file.toUri().getScheme()));
    }

    /**
     * if not use zero copy,must need chunkedWriter
     */
    public static void addChunkedWriter(final Connection c) {
        if (c.channel()
                    .pipeline()
                    .get(ChunkedWriteHandler.class) == null) {
            c.addHandlerLast(Netty.chunkedWriter, new ChunkedWriteHandler());
        }
    }

    public static ConnectionObserver compositeConnectionObserver(ConnectionObserver observer,
                                                                 ConnectionObserver other) {

        if (observer == ConnectionObserver.emptyListener()) {
            return other;
        }

        if (other == ConnectionObserver.emptyListener()) {
            return observer;
        }

        final ConnectionObserver[] newObservers;
        final ConnectionObserver[] thizObservers;
        final ConnectionObserver[] otherObservers;
        int length = 2;

        if (observer instanceof CompositeConnectionObserver) {
            thizObservers = ((CompositeConnectionObserver) observer).observers;
            length += thizObservers.length - 1;
        } else {
            thizObservers = null;
        }

        if (other instanceof CompositeConnectionObserver) {
            otherObservers = ((CompositeConnectionObserver) other).observers;
            length += otherObservers.length - 1;
        } else {
            otherObservers = null;
        }

        newObservers = new ConnectionObserver[length];

        int pos;
        if (thizObservers != null) {
            pos = thizObservers.length;
            System.arraycopy(thizObservers, 0,
                    newObservers, 0,
                    pos);
        } else {
            pos = 1;
            newObservers[0] = observer;
        }

        if (otherObservers != null) {
            System.arraycopy(otherObservers, 0,
                    newObservers, pos,
                    otherObservers.length);
        } else {
            newObservers[pos] = other;
        }

        return new CompositeConnectionObserver(newObservers);
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

    public static String format(Channel channel, String msg) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(msg, "msg");
        if (LOG_CHANNEL_INFO) {
            String channelStr;
            StringBuilder result;
            Connection connection = Connection.from(channel);
            if (connection instanceof ChannelOperationsId) {
                channelStr = ((ChannelOperationsId) connection).asLongText();
                if (channelStr.charAt(0) != TRACE_ID_PREFIX) {
                    result = new StringBuilder(1 + channelStr.length() + 2 + msg.length())
                            .append(CHANNEL_ID_PREFIX)
                            .append(channelStr)
                            .append(CHANNEL_ID_SUFFIX_1);
                } else {
                    result = new StringBuilder(channelStr.length() + 1 + msg.length())
                            .append(channelStr)
                            .append(CHANNEL_ID_SUFFIX_2);
                }
                return result.append(msg)
                        .toString();
            } else {
                channelStr = channel.toString();
                if (channelStr.charAt(0) == CHANNEL_ID_PREFIX) {
                    channelStr = channelStr.substring(ORIGINAL_CHANNEL_ID_PREFIX_LENGTH);
                    result = new StringBuilder(1 + channelStr.length() + 1 + msg.length())
                            .append(CHANNEL_ID_PREFIX)
                            .append(channelStr);
                } else {
                    int ind = channelStr.indexOf(ORIGINAL_CHANNEL_ID_PREFIX);
                    result = new StringBuilder(1 + (channelStr.length() - ORIGINAL_CHANNEL_ID_PREFIX_LENGTH) + 1 + msg.length())
                            .append(channelStr.substring(0, ind))
                            .append(CHANNEL_ID_PREFIX)
                            .append(channelStr.substring(ind + ORIGINAL_CHANNEL_ID_PREFIX_LENGTH));
                }
                return result.append(CHANNEL_ID_SUFFIX_2)
                        .append(msg)
                        .toString();
            }
        } else {
            return msg;
        }
    }

    public static final class CompositeConnectionObserver implements ConnectionObserver {

        final ConnectionObserver[] observers;

        CompositeConnectionObserver(ConnectionObserver[] observers) {
            this.observers = observers;
        }

        @Override
        public NvMap currentContext() {
            return observers[observers.length - 1].currentContext();
        }

        @Override
        public void onUncaughtException(Connection connection, Throwable error) {
            for (ConnectionObserver observer : observers) {
                observer.onUncaughtException(connection, error);
            }
        }

        @Override
        public void onStateChange(Connection connection, State newState) {
            for (ConnectionObserver observer : observers) {
                observer.onStateChange(connection, newState);
            }
        }
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