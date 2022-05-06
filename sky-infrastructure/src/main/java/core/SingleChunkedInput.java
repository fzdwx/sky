package core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedStream;

import java.io.ByteArrayInputStream;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/23 14:49
 */
public class SingleChunkedInput implements ChunkedInput<ByteBuf> {

    private final ByteBuf buf;
    private final long size;
    private boolean closed = false;
    private final static int maxSize = 1024 * 1024;

    private SingleChunkedInput(ByteBuf buf) {
        this.buf = buf;
        this.size = buf.readableBytes();
    }

    public static ChunkedInput<ByteBuf> of(byte[] bytes, final int chunkSize) {
        if (bytes.length > maxSize) {
            return new ChunkedStream(new ByteArrayInputStream(bytes), chunkSize);
        }
        return new SingleChunkedInput(Netty.wrap(bytes));
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        if (closed) return true;

        return buf.isReadable();
    }

    @Override
    public void close() throws Exception {
        closed = true;
        buf.release();
    }

    @Override
    public ByteBuf readChunk(final ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public ByteBuf readChunk(final ByteBufAllocator allocator) throws Exception {
        if (isEndOfInput()) {
            return null;
        }
        return this.buf;
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public long progress() {
        return size - buf.readableBytes();
    }
}