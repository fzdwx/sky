package core;

import exception.ChannelException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedStream;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/03/23 14:26:54
 */
@Slf4j
public abstract class ChannelOutBound implements NettyOutbound {

    private final Channel ch;

    public ChannelOutBound(Channel ch) {
        this.ch = ch;
    }

    @Override
    public Channel channel() {
        return this.ch;
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.ch.alloc();
    }

    @Override
    public NettyOutbound send(final ByteBuf data, boolean flush) {

        if (!channel().isActive()) {
            return then(ChannelException.beforeSend());
        }

        final Object msg = wrapData(data);

        if (flush) {
            return then(this.ch.writeAndFlush(msg));
        }

        return this.then(this.ch.write(msg));
    }

    @Override
    public NettyOutbound sendChunk(final InputStream in, final int chunkSize) {
        if (!channel().isActive()) {
            return then(ChannelException.beforeSend());
        }

        return then(ch.writeAndFlush(wrapChunkData(in, chunkSize)));
    }

    /**
     * when write data to peer,will call this method wrap data
     *
     * @see #send(ByteBuf, boolean)
     */
    public Object wrapData(final ByteBuf data) {
        return data;
    }

    /**
     * when write chunk data to peer,will call this method wrap data
     *
     * @see #sendChunk(InputStream, int)
     */
    public ChunkedInput<?> wrapChunkData(final InputStream in, final int chunkSize) {
        if (in instanceof ReadableByteChannel) {
            return new ChunkedNioStream((ReadableByteChannel) in, chunkSize);
        }
        return new ChunkedStream(in, chunkSize);
    }

}