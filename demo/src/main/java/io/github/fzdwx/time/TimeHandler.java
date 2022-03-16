package io.github.fzdwx.time;

import io.github.fzdwx.util.Buf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 22:03
 */
public class TimeHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        final var buf = Buf.allocInt()
                .writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        ctx.writeAndFlush(buf)
                .addListener((ChannelFutureListener) future -> {
                    ctx.close();
                });
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}