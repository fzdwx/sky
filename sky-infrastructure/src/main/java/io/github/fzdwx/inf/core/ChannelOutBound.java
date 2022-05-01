package io.github.fzdwx.inf.core;

import io.github.fzdwx.inf.core.exception.ChannelException;
import io.github.fzdwx.lambada.Exceptions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/03/23 14:26:54
 */
@Slf4j
public abstract class ChannelOutBound implements NettyOutbound, Connection, ChannelOperationsId {

    private final Channel ch;
    private final String shortId;
    String longId;
    boolean localActive;


    public ChannelOutBound(Channel ch) {
        this.ch = ch;
        this.shortId = ch.id().asShortText();
    }


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

        final var msg = wrapData(data);

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

    @Override
    public <S> NettyOutbound sendUsing(final Callable<? extends S> sourceInput, final BiFunction<? super Connection, ? super S, ?> mappedInput,
                                       final Consumer<? super S> sourceCleanup) {
        S source = null;
        try {
            source = sourceInput.call();
            return then(h -> {
                try {
                    this.ch.writeAndFlush(mappedInput.apply(this, sourceInput.call())).addListener(this::onOutboundComplete);
                } catch (Exception e) {
                    throw Exceptions.propagate(e);
                }
            });
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
        if (in instanceof ReadableByteChannel ins) {
            return new ChunkedNioStream(ins, chunkSize);
        }
        return new ChunkedStream(in, chunkSize);
    }

    @Override
    public String asShortText() {
        return shortId;
    }

    @Override
    public String asLongText() {
        boolean active = channel().isActive();
        if (localActive == active && longId != null) {
            return longId;
        }

        SocketAddress remoteAddress = channel().remoteAddress();
        SocketAddress localAddress = channel().localAddress();
        String shortText = asShortText();
        if (remoteAddress != null) {
            String localAddressStr = String.valueOf(localAddress);
            String remoteAddressStr = String.valueOf(remoteAddress);
            StringBuilder buf =
                    new StringBuilder(shortText.length() + 4 + localAddressStr.length() + 3 + 2 + remoteAddressStr.length())
                            .append(shortText)
                            .append(", L:")
                            .append(localAddressStr)
                            .append(active ? " - " : " ! ")
                            .append("R:")
                            .append(remoteAddressStr);
            longId = buf.toString();
        } else if (localAddress != null) {
            String localAddressStr = String.valueOf(localAddress);
            StringBuilder buf = new StringBuilder(shortText.length() + 4 + localAddressStr.length())
                    .append(shortText)
                    .append(", L:")
                    .append(localAddressStr);
            longId = buf.toString();
        } else {
            longId = shortText;
        }

        localActive = active;
        return longId;
    }

    /**
     * React on inbound/outbound completion (last packet)
     *
     * @param f
     */
    protected void onOutboundComplete(final Future<? super Void> f) {
        markPersistent(false);
        terminate();
    }

    /**
     * Final release/close (last packet)
     */
    protected final void terminate() {
        // nothing to do
    }
}