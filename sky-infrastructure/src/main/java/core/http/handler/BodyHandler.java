package core.http.handler;

import core.http.inter.AggHttpServerRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpExpectationFailedEvent;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import util.Netty;

import static com.google.common.net.HttpHeaders.EXPECT;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static util.Netty.getContentLength;

/**
 * content aggregation(parse data while reading)
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/28 20:45
 */
@Slf4j
public class BodyHandler extends ChannelInboundHandlerAdapter {

    private static final FullHttpResponse EXPECTATION_FAILED = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED, Unpooled.EMPTY_BUFFER);
    private static final FullHttpResponse TOO_LARGE_CLOSE = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, Unpooled.EMPTY_BUFFER);
    private static final FullHttpResponse CONTINUE =
            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
    private static final FullHttpResponse TOO_LARGE = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, Unpooled.EMPTY_BUFFER);

    static {
        EXPECTATION_FAILED.headers().set(CONTENT_LENGTH, 0);
        TOO_LARGE.headers().set(CONTENT_LENGTH, 0);

        TOO_LARGE_CLOSE.headers().set(CONTENT_LENGTH, 0);
        TOO_LARGE_CLOSE.headers().set(CONNECTION, HttpHeaderValues.CLOSE);
    }

    private final int maxContentLength;
    private AggHttpServerRequest currentRequest;

    public BodyHandler(final int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public static BodyHandler create() {
        return new BodyHandler(Netty.DEFAULT_MAX_CONTENT_LENGTH);
    }

    public static BodyHandler create(int maxContentLength) {
        return new BodyHandler(maxContentLength);
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        try {
            super.handlerRemoved(ctx);
        } finally {
            releaseCurrentMessage();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            // release current message if it is not null as it may be a left-over
            super.channelInactive(ctx);
        } finally {
            releaseCurrentMessage();
        }
    }

    @Override
    public void channelRead(@NotNull final ChannelHandlerContext ctx, @NotNull final Object msg) throws Exception {
        if (!(msg instanceof HttpObject)) {
            ReferenceCountUtil.release(msg);
        }

        if (msg instanceof DefaultHttpRequest) {
            if (currentRequest != null) {
                currentRequest.release();
                currentRequest = null;
                throw new IllegalStateException("unexpected message aggregate");
            }

            final DefaultHttpRequest nettyRequest = (DefaultHttpRequest) msg;
            if (!continueResponse(nettyRequest, ctx)) {
                return;
            }

            if (isContentLengthInvalid(nettyRequest)) {
                handlerContentLengthInvalid(ctx, msg);
                return;
            }

            CompositeByteBuf content = ctx.alloc().compositeBuffer();

            currentRequest = new AggHttpServerRequest(nettyRequest, content);
        } else if (msg instanceof HttpContent) {
            if (currentRequest == null) {
                // it is possible that a TooLongFrameException was already thrown but we can still discard data
                // until the begging of the next request/response.
                return;
            }

            final ByteBuf content = currentRequest.content();

            final HttpContent m = (HttpContent) msg;

            if (content.readableBytes() > maxContentLength - m.content().readableBytes()) {
                // By convention, full message type extends first message type.
                final AggHttpServerRequest request = currentRequest;
                handlerContentLengthInvalid(ctx, request);
                return;
            }

            currentRequest.offer(m);

            if (m instanceof LastHttpContent) {
                finishAggregation(currentRequest);
                ctx.fireChannelRead(currentRequest);
                currentRequest = null;
            }
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // We might need keep reading the channel until the full message is aggregated.
        //
        // See https://github.com/netty/netty/issues/6583
        if (currentRequest != null && !ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
        ctx.fireChannelReadComplete();
    }

    protected boolean isContentLengthInvalid(HttpMessage start) {
        try {
            return getContentLength(start, -1L) > maxContentLength;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private void releaseCurrentMessage() {
        if (currentRequest != null) {
            currentRequest.destroy();
            currentRequest = null;
        }
    }

    private void finishAggregation(final AggHttpServerRequest aggregated) {
        if (!HttpUtil.isContentLengthSet(aggregated.nettyRequest())) {
            aggregated.headers().set(
                    CONTENT_LENGTH,
                    String.valueOf(aggregated.content().readableBytes()));
        }
    }

    private void handlerContentLengthInvalid(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        currentRequest = null;
        try {
            ctx.writeAndFlush(TOO_LARGE.retainedDuplicate()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        log.debug("Failed to send a 413 Request Entity Too Large.", future.cause());
                        ctx.close();
                    }
                }
            });
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private boolean continueResponse(DefaultHttpRequest start, ChannelHandlerContext ctx) {
        if (start.method() == HttpMethod.OPTIONS) {
            return true;
        }

        final FullHttpResponse response;
        if (Netty.isUnsupportedExpectation(start)) {
            // if the request contains an unsupported expectation, we return 417
            ctx.fireUserEventTriggered(HttpExpectationFailedEvent.INSTANCE);
            response = EXPECTATION_FAILED.retainedDuplicate();
        } else if (HttpUtil.is100ContinueExpected(start)) {
            // if the request contains 100-continue but the content-length is too large, we return 413
            if (getContentLength(start, -1L) <= maxContentLength) {
                response = CONTINUE.retainedDuplicate();
            } else {
                ctx.fireUserEventTriggered(HttpExpectationFailedEvent.INSTANCE);
                response = TOO_LARGE.retainedDuplicate();
            }
        } else response = null;


        if (response != null) {
            start.headers().remove(EXPECT);
            ctx.writeAndFlush(response).addListener(Netty.close);
            return false;
        }

        return true;
    }
}