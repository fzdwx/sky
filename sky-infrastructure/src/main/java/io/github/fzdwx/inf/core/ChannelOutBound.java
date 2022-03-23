package io.github.fzdwx.inf.core;

import io.github.fzdwx.inf.core.exception.ChannelException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedStream;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/03/23 14:26:54
 */
public class ChannelOutBound implements NettyOutbound, Connection {

    private final Channel ch;

    /**
     * Return the current {@link Channel} bound {@link ChannelOutBound} or null if none
     *
     * @param ch the current {@link Channel}
     * @return the current {@link Channel} bound {@link ChannelOutBound} or null if none
     */
    @Nullable
    public static ChannelOutBound get(Channel ch) {
        final var connection = Connection.from(ch);
        if (ChannelOutBound.class.isAssignableFrom(Connection.class)) {
            return (ChannelOutBound) connection;
        }
        return null;
    }

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

        if (flush) {
            return then(this.ch.writeAndFlush(wrapData(data)));
        }

        return this.then(this.ch.write(wrapData(data)));
    }

    @Override
    public NettyOutbound sendChunk(final InputStream in, final int chunkSize) {
        if (!channel().isActive()) {
            return then(ChannelException.beforeSend());
        }

        return then(ch.writeAndFlush(wrapChunkData(in, chunkSize)));
    }

    public Object wrapData(final ByteBuf data) {
        return data;
    }

    public ChunkedInput<ByteBuf> wrapChunkData(final InputStream in, final int chunkSize) {
        if (in instanceof ReadableByteChannel ins) {
            return new ChunkedNioStream(ins, chunkSize);
        }
        return new ChunkedStream(in, chunkSize);
    }

    @Override
    public <S> NettyOutbound sendUsing(final Callable<? extends S> sourceInput, final BiFunction<? super Connection, ? super S, ?> mappedInput,
                                       final Consumer<? super S> sourceCleanup) {
        S source = null;
        try {
            source = sourceInput.call();
            return then(this.ch.writeAndFlush(mappedInput.apply(this, sourceInput.call())));
        } catch (Exception e) {
            return then(e);
        } finally {
            if (source != null) {
                sourceCleanup.accept(source);
            }
        }
    }

    @Override
    public ChannelOutBound withConnection(final Consumer<? super Connection> connection) {
        connection.accept(this);
        return this;
    }
}