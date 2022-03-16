package io.github.fzdwx.time.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 22:15
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final var in = (ByteBuf) msg;

        System.out.println(new Date(((in.readUnsignedInt() - 2208988800L) * 1000L)));

        ctx.close();

        in.release();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();

        ctx.close();
    }
}