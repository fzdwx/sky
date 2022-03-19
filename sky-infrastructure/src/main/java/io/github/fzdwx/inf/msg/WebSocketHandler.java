package io.github.fzdwx.inf.msg;

import io.github.fzdwx.inf.Listener;
import io.github.fzdwx.inf.route.msg.SocketSession;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * handler websocket frame.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 20:37
 * @since 0.06
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final Listener listener;
    private final SocketSession session;

    public WebSocketHandler(final Listener listener, final SocketSession session) {
        this.listener = listener;
        this.session = session;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        listener.onclose(session);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        listener.onEvent(session, evt);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        listener.onError(session, cause);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final WebSocketFrame msg) throws Exception {
        if (msg instanceof TextWebSocketFrame text) {
            listener.onText(session, text.text());
            return;
        }
        if (msg instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(msg.content().retain()));
            return;
        }
        if (msg instanceof CloseWebSocketFrame) {
            ctx.writeAndFlush(msg.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (msg instanceof BinaryWebSocketFrame b) {
            listener.onBinary(session, b.content());
        }
    }
}