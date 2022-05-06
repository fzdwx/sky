package core;

import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.io.InputStream;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/23 10:11
 */
public interface NettyOutbound {

    Channel channel();

    ByteBufAllocator alloc();

    /**
     * send data to peer.
     */
    NettyOutbound send(ByteBuf data, boolean flush);

    NettyOutbound sendChunk(InputStream in, int chunkSize);

    default NettyOutbound send(ByteBuf data) {
        return send(data, false);
    }

    default NettyOutbound send(byte[] data) {
        return send(Netty.wrap(data));
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
        ChannelPromise cp = channel().newPromise();
        then().addListener(f -> {
            try {
                h.call(null);
                cp.setSuccess();
            } catch (Exception e) {
                cp.setFailure(e);
            }
        });
        return then(cp);
    }
}