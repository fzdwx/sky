package io.github.fzdwx.helloworld.serv;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;
import java.util.Date;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 15:27
 */
@ChannelHandler.Sharable
public class ServHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.write("It is " + new Date() + " now.\r\n");
        ctx.flush();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
        String response;
        boolean close = false;

        if (msg.isBlank()) {
            response = "Please type something.\r\n";
        } else if ("bye".equalsIgnoreCase(msg)) {
            response = "Have a good day!\r\n";
            close = true;
        } else {
            response = "Did you say '" + msg + "'?\r\n";
        }

        final var f = ctx.write(response);

        if (close) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}