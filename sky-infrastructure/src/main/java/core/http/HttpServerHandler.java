package core.http;

import core.http.ext.HttpExceptionHandler;
import core.http.ext.HttpHandler;
import core.http.inter.AggHttpServerRequest;
import core.serializer.JsonSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
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

    private AggHttpServerRequest currentAgg;

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
        if (handleRequest(ctx, msg)) {

        } else {
            ctx.writeAndFlush("Unsupported message type: " + msg.getClass().getName()).addListener(Netty.close);
            ReferenceCountUtil.release(msg);
        }
        // if (msg instanceof DefaultHttpRequest) {
        //     currentAgg = new AggHttpServerRequest((HttpRequest) msg, EMPTY_BUFFER);
        //     return;
        // } else if (msg instanceof HttpContent) {
        //     currentAgg.offer((HttpContent) msg);
        //     ReferenceCountUtil.release(msg);
        // }
        //
        // if (msg instanceof LastHttpContent) {
        //     HttpServerRequest request = HttpServerRequest.from(ctx, ssl, (currentAgg), serializer);
        //     final HttpServerResponse response = HttpServerResponse.create(ctx.channel(), request);
        //     httpHandler.handle(request, response);
        // }

    }


    @Override
    public void channelInactive(@NotNull final ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("http server handler exceptionCaught: {}", cause.getMessage());
        //    todo
    }

    public boolean handleRequest(final ChannelHandlerContext ctx, final Object msg) {
        HttpServerRequest request = null;
        if (msg instanceof AggHttpServerRequest) {
            request = HttpServerRequest.from(ctx, ssl, ((AggHttpServerRequest) msg), serializer);
        } else if (msg instanceof FullHttpRequest) {
            request = HttpServerRequest.create(ctx, ssl, ((FullHttpRequest) msg), httpDataFactory, serializer);
        }

        if (request == null) {
            return false;
        }

        final HttpServerResponse response = HttpServerResponse.create(ctx.channel(), request);
        try {
            httpHandler.handle(request, response);
        } catch (Exception e) {
            if (e.getClass().equals(InvocationTargetException.class)) {
                e = (Exception) e.getCause();
            }
            exceptionHandler.handler(request, response, e);
        } finally {
            // TODO: 2022/5/28 relaese
            // httpRequest.release();
        }
        return true;


    }

}