package core.http;

import util.Netty;
import core.http.ext.HttpExceptionHandler;
import core.http.ext.HttpHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import core.serializer.JsonSerializer;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final HttpHandler httpHandler;
    private final boolean ssl;
    private final HttpDataFactory httpDataFactory;
    private final HttpExceptionHandler exceptionHandler;
    private final JsonSerializer serializer;

    public HttpServerHandler(final HttpHandler httpHandler, final HttpExceptionHandler exceptionHandler, final Boolean ssl,
                             final HttpDataFactory httpDataFactory, final JsonSerializer serializer) {
        this.httpHandler = httpHandler;
        this.exceptionHandler = HttpExceptionHandler.defaultExceptionHandler(exceptionHandler);
        this.ssl = ssl;
        this.httpDataFactory = httpDataFactory == null ? new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE) : httpDataFactory;
        this.serializer = serializer == null ? JsonSerializer.codec : serializer;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                handleRequest(ctx, ((FullHttpRequest) msg));
            } else {
                ctx.writeAndFlush("Unsupported message type: " + msg.getClass().getName()).addListener(Netty.close);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("http server handler exceptionCaught: {}", cause.getMessage());

        super.exceptionCaught(ctx, cause);
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