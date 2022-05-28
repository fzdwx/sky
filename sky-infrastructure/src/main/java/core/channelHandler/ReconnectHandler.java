package core.channelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/12 10:25
 */
@Slf4j
public class ReconnectHandler extends ChannelInboundHandlerAdapter {

    private final static String CONNECTION_RESET_MESSAGE = "java.net.SocketException: Connection reset";

    private final Runnable reconnectFunc;

    public ReconnectHandler(Runnable reconnectFunc) {
        this.reconnectFunc = reconnectFunc;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        log.info("call reconnectFunc");
        reconnectFunc.run();
        //channelUnregistered
    }
}