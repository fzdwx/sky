package io.github.fzdwx.time;

import io.github.fzdwx.lambada.lang.UnixTime;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 11:54
 */
public class TimeEncoder extends MessageToByteEncoder<UnixTime> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final UnixTime msg, final ByteBuf out) throws Exception {
        out.writeInt((int) msg.value());
    }
}