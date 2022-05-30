package core.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelProgressiveFutureListener;

import java.io.InputStream;
import java.io.RandomAccessFile;

public class CopyOutbound implements Outbound {

    protected final Outbound source;

    public CopyOutbound(Outbound source) {
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
    public Outbound send(final ByteBuf data, final boolean flush) {
        return this.source.send(data, flush);
    }

    @Override
    public Outbound sendStream(final InputStream in, final int chunkSize) {
        return this.source.sendStream(in, chunkSize);
    }

    @Override
    public ChannelFuture sendFile(RandomAccessFile file, int chunkSize, final boolean flush,
                                  final ChannelProgressiveFutureListener channelProgressiveFutureListener) {
        return source.sendFile(file, chunkSize, flush, channelProgressiveFutureListener);
    }

    public static class NormalOutBoundThen extends CopyOutbound {

        private final ChannelFuture f;

        public NormalOutBoundThen(Outbound source, ChannelFuture future) {
            super(source);
            this.f = future;
        }

        @Override
        public ChannelFuture then() {
            return f;
        }
    }

    public static class ErrorOutBoundThen extends CopyOutbound {

        private final Throwable t;

        public ErrorOutBoundThen(Outbound source, Throwable t) {
            super(source);
            this.t = t;
        }

        @Override
        public ChannelFuture then() {
            return channel().newFailedFuture(t);
        }
    }
}