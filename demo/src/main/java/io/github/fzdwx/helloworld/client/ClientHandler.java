package io.github.fzdwx.helloworld.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 16:21
 */
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}