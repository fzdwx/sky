package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.HttpRequest;
import io.github.fzdwx.inf.http.HttpResponse;
import io.github.fzdwx.inf.msg.WebSocketHandler;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
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
    public void handleRequest(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        // find the handler for the request path and method
        final var httpRequest = HttpRequest.create(ctx, request);

        final var handler = router.matchOne(httpRequest);

        if (handler == null) { // handler not found
            ctx.writeAndFlush(HttpResult.make(HttpResponseStatus.NOT_FOUND)).addListener(Netty.close);
            return;
        }

        try {
            // handle the request
            handler.handle(httpRequest, HttpResponse.create(ctx.channel()));
        } catch (Exception e) {
            ctx.writeAndFlush(HttpResult.fail(e)).addListener(Netty.close);
        }
    }
}