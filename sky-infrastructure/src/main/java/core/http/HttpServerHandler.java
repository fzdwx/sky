package core.http;

import core.http.ext.HttpExceptionHandler;
import core.http.ext.HttpHandler;
import core.http.inter.HttpServerRequestImpl;
import core.http.inter.HttpServerResponseImpl;
import core.serializer.JsonSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import util.Netty;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final HttpHandler httpHandler;
    private final boolean ssl;
    private final HttpDataFactory httpDataFactory;
    private final HttpExceptionHandler exceptionHandler;
    private final JsonSerializer serializer;

    private HttpServerRequestImpl requestIng;
    private HttpServerResponseImpl responseIng;


    public HttpServerHandler(
            final HttpHandler httpHandler,
            final HttpExceptionHandler exceptionHandler,
            final Boolean ssl,
            final HttpDataFactory httpDataFactory,
            final JsonSerializer serializer) {
        this.httpHandler = httpHandler;
        this.exceptionHandler = HttpExceptionHandler.defaultExceptionHandler(exceptionHandler);
        this.ssl = ssl;
        this.httpDataFactory = httpDataFactory == null ? new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE) : httpDataFactory;
        this.serializer = serializer == null ? JsonSerializer.codec : serializer;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleRequest(ctx, ((FullHttpRequest) msg));
        } else {
            ctx.writeAndFlush("Unsupported message type: " + msg.getClass().getName()).addListener(Netty.close);
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(@NotNull final ChannelHandlerContext ctx) throws Exception {
        this.requestIng = null;
        // todo 销毁
        this.responseIng = null;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("http server handler exceptionCaught: {}", cause.getMessage());
        final HttpServerRequest requestIng = this.requestIng;
        final HttpServerResponse responseIng = this.responseIng;

        if (requestIng != null && responseIng != null) {
            exceptionHandler.handler(requestIng, responseIng, cause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * 处理 client request
     *
     * @param ctx     ctx
     * @param request http request
     */
    public void handleRequest(final ChannelHandlerContext ctx, final FullHttpRequest request) {

        final HttpServerRequest httpRequest = HttpServerRequest.create(ctx, ssl, request, httpDataFactory, serializer);
        final HttpServerResponse response = HttpServerResponse.create(ctx.channel(), httpRequest);

        try {
            httpHandler.handle(httpRequest, response);
        } catch (Exception e) {
            if (e.getClass().equals(InvocationTargetException.class)) {
                e = (Exception) e.getCause();
            }
            exceptionHandler.handler(httpRequest, response, e);
        } finally {
            httpRequest.release();
        }
    }

}