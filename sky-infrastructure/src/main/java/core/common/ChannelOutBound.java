package core.common;

import exception.ChannelException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.ReadableByteChannel;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/03/23 14:26:54
 */
@Slf4j
public abstract class ChannelOutBound implements Outbound {

    protected final Channel ch;

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
    public Outbound send(final ByteBuf data, boolean flush) {

        if (!channel().isActive()) {
            return then(ChannelException.beforeSend());
        }

        final Object msg = wrapData(data);

        if (flush) {
            return then(this.ch.writeAndFlush(msg));
        }

        return this.then(this.ch.write(msg));
    }

    /**
     * when write data to peer,will call this method wrap data
     *
     * @see #send(ByteBuf, boolean)
     */
    public Object wrapData(final ByteBuf data) {
        return data;
    }

    @Override
    public Outbound sendStream(final InputStream in, final int chunkSize) {
        if (!channel().isActive()) {
            return then(ChannelException.beforeSend());
        }

        return then(ch.writeAndFlush(wrapStreamData(in, chunkSize)));
    }

    /**
     * when write chunk data to peer,will call this method wrap data
     *
     * @see #sendStream(InputStream, int)
     */
    public ChunkedInput<?> wrapStreamData(final InputStream in, final int chunkSize) {
        if (in instanceof ReadableByteChannel) {
            return new ChunkedNioStream((ReadableByteChannel) in, chunkSize);
        }
        return new ChunkedStream(in, chunkSize);
    }

    @Override
    public ChannelFuture sendFile(final RandomAccessFile file, final int chunkSize, final boolean flush,
                                  final ChannelProgressiveFutureListener channelProgressiveFutureListener) {
        if (!channel().isActive()) {
            return then(ChannelException.beforeSend()).then();
        }

        if (flush) {
            return ch.writeAndFlush(wrapFile(file, chunkSize), ch.newProgressivePromise()).addListener(channelProgressiveFutureListener);
        }
        return ch.write(wrapFile(file, chunkSize), ch.newProgressivePromise()).addListener(channelProgressiveFutureListener);
    }

    /**
     * when write file to peer,will call this method wrap data
     */
    @SneakyThrows
    public Object wrapFile(final RandomAccessFile file, final int chunkSize) {
        return new ChunkedFile(file, chunkSize);
    }
}