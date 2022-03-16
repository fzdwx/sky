package io.github.fzdwx.util;

import io.netty.buffer.ByteBuf;

/**
 * byteBuf tool.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 21:44
 */
public final class Buf {

    public static String read(ByteBuf buf) {
        final var dest = new byte[buf.readableBytes()];

        buf.readBytes(dest);

        return new String(dest);
    }
}