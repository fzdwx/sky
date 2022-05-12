package core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/12 10:25
 */
public class ReconnectHandler extends ChannelInboundHandlerAdapter {

    private final static String CONNECTION_RESET_MESSAGE = "java.net.SocketException: Connection reset";

    private final Runnable reconnectFunc;

    public ReconnectHandler(Runnable reconnectFunc) {
        this.reconnectFunc = reconnectFunc;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        // if (Lang.eq(cause.toString(), CONNECTION_RESET_MESSAGE)) {
        //     reconnectFunc.run();
        // }
        System.out.println("error");
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("run reconnect func");
        reconnectFunc.run();
        //channelUnregistered
    }
}