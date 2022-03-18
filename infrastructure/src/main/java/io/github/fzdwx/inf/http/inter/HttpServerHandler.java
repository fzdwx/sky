package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.HttpResponse;
import io.github.fzdwx.inf.http.inter.HttpResult;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 17:45
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private final Router router;

    public HttpServerHandler(final Router router) {
        this.router = router;
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

    /**
     * 处理 client request
     *
     * @param ctx     ctx
     * @param request http request
     */
    public void handleRequest(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        // find the handler for the request path and method
        final var handler = router.matchOne(request, RequestMethod.of(request));
        if (handler == null) { // handler not found
            ctx.writeAndFlush(HttpResult.make(HttpResponseStatus.NOT_FOUND)).addListener(Netty.close);
            return;
        }

        // handle the request
        handler.handle(request, HttpResponse.create(ctx.channel()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}