package http;

import core.Netty;
import http.ext.HttpExceptionHandler;
import http.ext.HttpRequestConsumer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final HttpRequestConsumer consumer;
    private final boolean ssl;
    private final HttpDataFactory httpDataFactory;
    private final HttpExceptionHandler exceptionHandler;

    public HttpServerHandler(final HttpRequestConsumer consumer, final HttpExceptionHandler exceptionHandler, final Boolean ssl,
                             final HttpDataFactory httpDataFactory) {
        this.consumer = consumer;
        this.exceptionHandler = HttpExceptionHandler.defaultExceptionHandler(exceptionHandler);
        this.ssl = ssl;
        this.httpDataFactory = httpDataFactory == null ? new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE) : httpDataFactory;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest request) {
                handleRequest(ctx, request);
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
        // TODO: 2022/5/10 日志级别
        super.exceptionCaught(ctx, cause);
    }

    /**
     * 处理 client request
     *
     * @param ctx     ctx
     * @param request http request
     */
    @SneakyThrows
    public void handleRequest(final ChannelHandlerContext ctx, final FullHttpRequest request) {

        final var httpRequest = HttpServerRequest.create(ctx, ssl, request, httpDataFactory);
        final var response = HttpServerResponse.create(ctx.channel(), httpRequest);

        try {
            consumer.consume(httpRequest, response);
        } catch (Exception e) {
            exceptionHandler.handler(httpRequest, response, e);
        } finally {
            httpRequest.release();
        }
    }
}