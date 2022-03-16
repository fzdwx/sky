package io.github.fzdwx.echo;

import io.github.fzdwx.discard.DiscardHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 21:51
 */
public class EchoHandler extends DiscardHandler {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ctx.write(msg);
        ctx.flush();
    }
}