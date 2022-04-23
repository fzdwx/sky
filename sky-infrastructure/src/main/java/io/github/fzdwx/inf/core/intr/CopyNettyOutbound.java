package io.github.fzdwx.inf.core.intr;

import io.github.fzdwx.inf.core.Connection;
import io.github.fzdwx.inf.core.NettyOutbound;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public  class CopyNettyOutbound implements NettyOutbound {

    protected final NettyOutbound source;

    public CopyNettyOutbound(NettyOutbound source) {
        this.source = source;
    }

    @Override
    public Channel channel() {
        return source.channel();
    }

    @Override
    public ByteBufAllocator alloc() {
        return source.alloc();
    }

    @Override
    public NettyOutbound send(final ByteBuf data, final boolean flush) {
        return this;
    }

    @Override
    public NettyOutbound sendChunk(final InputStream in, final int chunkSize) {
        return this;
    }

    @Override
    public <S> NettyOutbound sendUsing(final Callable<? extends S> sourceInput, final BiFunction<? super Connection, ? super S, ?> mappedInput,
                                       final Consumer<? super S> sourceCleanup) {
        return source.sendUsing(sourceInput, mappedInput, sourceCleanup);
    }

    @Override
    public NettyOutbound withConnection(final Consumer<? super Connection> withConnection) {
        return source.withConnection(withConnection);
    }

    @Override
    public int chunkSize() {
        throw new UnsupportedOperationException();
    }

    public static class NormalOutBoundThen extends CopyNettyOutbound {

        private final ChannelFuture f;

        public NormalOutBoundThen(NettyOutbound source, ChannelFuture future) {
            super(source);
            this.f = future;
        }

        @Override
        public ChannelFuture then() {
            return f;
        }
    }

    public static class ErrorOutBoundThen extends CopyNettyOutbound {

        private final Throwable t;

        public ErrorOutBoundThen(NettyOutbound source, Throwable t) {
            super(source);
            this.t = t;
        }

        @Override
        public ChannelFuture then() {
            return channel().newFailedFuture(t);
        }
    }
}