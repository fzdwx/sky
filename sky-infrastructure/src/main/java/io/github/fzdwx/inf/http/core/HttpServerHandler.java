package io.github.fzdwx.inf.http.core;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.inter.HttpResult;
import io.github.fzdwx.inf.route.Router;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * http server handler.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 17:45
 * @since 0.06
 */
@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final Router router;
    private final boolean ssl;
    private final HttpDataFactory httpDataFactory;

    public HttpServerHandler(final Router router, final Boolean ssl, final HttpDataFactory httpDataFactory) {
        this.router = router;
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
        // find the handler for the request path and method
        final var httpRequest = HttpServerRequest.create(ctx, ssl, request, httpDataFactory);

        final var handler = router.matchOne(httpRequest);

        if (handler == null) { // handler not found
            ctx.writeAndFlush(HttpResult.make(HttpResponseStatus.NOT_FOUND)).addListener(Netty.close);
            return;
        }

        try {
            // handle the request
            handler.handle(httpRequest, HttpServerResponse.create(ctx.channel(), httpRequest));
        } catch (Exception e) {
            ctx.writeAndFlush(HttpResult.fail(e)).addListener(Netty.close);
            throw e;
        } finally {
            httpRequest.release();
        }
    }
}