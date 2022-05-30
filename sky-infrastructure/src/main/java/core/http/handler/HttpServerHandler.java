package core.http.handler;

import core.http.ext.HttpExceptionHandler;
import core.http.ext.HttpHandler;
import core.http.ext.HttpServerRequest;
import core.http.ext.HttpServerResponse;
import core.http.inter.AggHttpServerRequest;
import core.serializer.JsonSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import util.Netty;

@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final HttpHandler httpHandler;
    private final boolean ssl;
    private final HttpExceptionHandler exceptionHandler;
    private final JsonSerializer serializer;

    public HttpServerHandler(
            final HttpHandler httpHandler,
            final HttpExceptionHandler exceptionHandler,
            final Boolean ssl,
            final JsonSerializer serializer) {
        this.ssl = ssl;
        this.httpHandler = httpHandler;
        this.exceptionHandler = HttpExceptionHandler.defaultExceptionHandler(exceptionHandler);
        this.serializer = serializer == null ? JsonSerializer.codec : serializer;
    }

    @Override
    public void channelInactive(@NotNull final ChannelHandlerContext ctx) throws Exception {
        final HttpServerRequest request = ctx.channel().attr(Netty.REQUEST_KEY).getAndSet(null);
        final HttpServerResponse response = ctx.channel().attr(Netty.RESPONSE_KEY).getAndSet(null);

        if (request != null) {
            log.debug("http server handler channelInactive and destroy request and response");
            request.destroy();
        }

    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (!handleRequest(ctx, msg)) {
            ctx.writeAndFlush("Unsupported message type: " + msg.getClass().getName()).addListener(Netty.close);
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("http server handler exceptionCaught: {}", cause.getMessage(), cause);
        final HttpServerRequest request = ctx.channel().attr(Netty.REQUEST_KEY).getAndSet(null);
        final HttpServerResponse response = ctx.channel().attr(Netty.RESPONSE_KEY).getAndSet(null);
        if (request == null) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Netty.wrap(ctx.alloc(), cause.getMessage())))
                    .addListener(Netty.close);
        } else {
            log.debug("http server handler exceptionCaught and destroy request and response");

            if (response != null) {
                exceptionHandler.handler(request, response, cause);
                request.destroy();
            }

            request.destroy();
        }
    }

    public boolean handleRequest(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof FullHttpRequest) {
            final HttpServerRequest request = HttpServerRequest.create(ctx, ssl, ((AggHttpServerRequest) msg), serializer);
            final HttpServerResponse response = HttpServerResponse.create(ctx.channel(), request);

            try {
                httpHandler.handle(request, response);
            } catch (Exception e) {
                exceptionHandler.handler(request, response, e);
            } finally {
                ctx.channel().attr(Netty.REQUEST_KEY).set(request);
                ctx.channel().attr(Netty.RESPONSE_KEY).set(response);
            }

            return true;
        }

        return false;
    }

}