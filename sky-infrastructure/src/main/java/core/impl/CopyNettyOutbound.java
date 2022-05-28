package core.impl;

import core.NettyOutbound;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.InputStream;

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