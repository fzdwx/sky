package core.common;

import io.github.fzdwx.lambada.anno.NonNull;
import io.github.fzdwx.lambada.anno.Nullable;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.ChannelPromise;
import util.Netty;

import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/23 10:11
 */
public interface Outbound {

    Channel channel();

    ByteBufAllocator alloc();

    /**
     * send data to peer.
     */
    Outbound send(ByteBuf data, boolean flush);

    Outbound sendStream(InputStream in, int chunkSize);

    /**
     * send file
     *
     * @apiNote only {@link core.http.ext.HttpServerResponse} correctly implement this method
     */
    ChannelFuture sendFile(@NonNull final RandomAccessFile file,
                           final int chunkSize, final boolean flush,
                           @Nullable final ChannelProgressiveFutureListener channelProgressiveFutureListener);

    /**
     * send file and set chunk size is {@link Netty#DEFAULT_CHUNK_SIZE} (8192)
     */
    default ChannelFuture sendFile(RandomAccessFile file) {
        return sendFile(file, Netty.DEFAULT_CHUNK_SIZE);
    }

    default ChannelFuture sendFile(final RandomAccessFile file, final int chunkSize,
                                   final ChannelProgressiveFutureListener channelProgressiveFutureListener) {
        return sendFile(file, chunkSize, false, channelProgressiveFutureListener);
    }

    default ChannelFuture sendFile(final RandomAccessFile file, final int chunkSize) {
        return sendFile(file, chunkSize, false, null);
    }

    default ChannelFuture sendFileAndFlush(final RandomAccessFile file, final int chunkSize,
                                           final ChannelProgressiveFutureListener channelProgressiveFutureListener) {
        return sendFile(file, chunkSize, true, channelProgressiveFutureListener);
    }

    /**
     * @see #sendFile(RandomAccessFile, int, boolean, ChannelProgressiveFutureListener)
     */
    default ChannelFuture sendFileAndFlush(final RandomAccessFile file, final int chunkSize) {
        return sendFile(file, chunkSize, true, null);
    }

    default Outbound send(ByteBuf data) {
        return send(data, false);
    }

    default Outbound send(byte[] data) {
        return send(Netty.wrap(alloc(), data));
    }

    default Outbound sendAndFlush(byte[] data) {
        return send(Netty.wrap(alloc(), data), true);
    }

    default Outbound then(Throwable t) {
        return new CopyOutbound.ErrorOutBoundThen(this, t);
    }

    default Outbound then(final ChannelFuture f) {
        return new CopyOutbound.NormalOutBoundThen(this, f);
    }

    default ChannelFuture then() {
        return channel().newSucceededFuture();
    }

    default Outbound then(Hooks<Void> h) {
        ChannelPromise cp = channel().newPromise();
        then().addListener(f -> {
            try {
                cp.setSuccess();
            } catch (Exception e) {
                cp.setFailure(e);
            }
        });
        h.call(null);
        return then(cp);
    }
}