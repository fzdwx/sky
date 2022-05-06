package http;

import core.Netty;
import http.inter.HttpResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final HttpRequestConsumer consumer;
    private final boolean ssl;
    private final HttpDataFactory httpDataFactory;

    public HttpServerHandler(final HttpRequestConsumer consumer, final Boolean ssl, final HttpDataFactory httpDataFactory) {
        this.consumer = consumer;
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
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
            //  todo custom error handler
            ctx.writeAndFlush(HttpResult.fail(e)).addListener(Netty.close);
            throw e;
        } finally {
            httpRequest.release();
        }
    }
}