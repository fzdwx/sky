package io.github.fzdwx.inf.core;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.core.intr.CopyNettyOutbound;
import io.github.fzdwx.lambada.Exceptions;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/23 10:11
 */
public interface NettyOutbound {

    Channel channel();

    ByteBufAllocator alloc();

    /**
     * send data to peer.
     */
    NettyOutbound send(ByteBuf data, boolean flush);

    NettyOutbound sendChunk(InputStream in,int chunkSize);

    /**
     * Binds a send to a starting/cleanup lifecycle
     * <p>Note: Nesting any send* method is not supported.</p>
     *
     * @param sourceInput   state generator
     * @param mappedInput   input to send
     * @param sourceCleanup state cleaner
     * @param <S>           state type
     * @return a new {@link NettyOutbound}
     */
    <S> NettyOutbound sendUsing(Callable<? extends S> sourceInput,
                                BiFunction<? super Connection, ? super S, ?> mappedInput,
                                Consumer<? super S> sourceCleanup);

    default  NettyOutbound send(ByteBuf data) {
        return send(data, false);
    }

    default NettyOutbound send(byte[] data) {
        return send(Netty.wrap(data));
    }

    default NettyOutbound sendFile(Path file) {
        try {
            return sendFile(file, 0L, Files.size(file));
        } catch (IOException e) {
            return then(e);
        }
    }

    default NettyOutbound then(Throwable t) {
        return new CopyNettyOutbound.ErrorOutBoundThen(this, t);
    }

    default NettyOutbound then(final ChannelFuture f) {
        return new CopyNettyOutbound.NormalOutBoundThen(this, f);
    }

    default ChannelFuture then() {
        return channel().newSucceededFuture();
    }

    default NettyOutbound then(Hooks<Void> h) {
        h.call(null);
        return this;
    }

    NettyOutbound withConnection(Consumer<? super Connection> withConnection);

    /**
     * send file to peer. if not use SSL/TLS,will use zero copy.
     *
     * @param file     source file
     * @param position file start position
     * @param count    how mush you want send
     */
    default NettyOutbound sendFile(Path file, long position, long count) {
        Objects.requireNonNull(file, "filepath");

        return sendUsing(() -> FileChannel.open(file, StandardOpenOption.READ),
                (c, fc) -> {
                    if (Netty.mustChunkFileTransfer(c, file)) {
                        Netty.addChunkedWriter(c);
                        try {
                            return new ChunkedNioFile(fc, position, count, 1024);
                        } catch (Exception ioe) {
                            throw Exceptions.propagate(ioe);
                        }
                    }
                    // use zero copy
                    return new DefaultFileRegion(fc, position, count);
                },
                Netty.fileCloser);
    }

    /**
     * will not use zero copy.
     */
    default NettyOutbound sendFileChunked(Path file, long position, long count) {
        Objects.requireNonNull(file, "filepath");

        return sendUsing(() -> FileChannel.open(file, StandardOpenOption.READ),
                (c, fc) -> {
                    Netty.addChunkedWriter(c);
                    try {
                        return new ChunkedNioFile(fc, position, count, 1024);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                },
                Netty.fileCloser);
    }
}