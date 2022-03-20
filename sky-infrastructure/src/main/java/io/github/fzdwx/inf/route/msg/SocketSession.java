package io.github.fzdwx.inf.route.msg;

import io.github.fzdwx.inf.route.msg.inter.SocketSessionImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * session.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 13:11
 * @since 0.06
 */
public interface SocketSession {

    Channel channel();

    static SocketSession create(Channel channel) {
        return new SocketSessionImpl(channel);
    }

    /**
     * close session.
     *
     * @since 0.07
     */
    ChannelFuture reject();

    /**
     * close session.
     *
     * @since 0.07
     */
    ChannelFuture reject(String text);

    /**
     * send text to client.
     *
     * @since 0.06
     */
    ChannelFuture send(String text);

    /**
     * send text(bytes) to client.
     *
     * @since 0.06
     */
    ChannelFuture send(byte[] text);

    /**
     * send binary(like file) to client.
     *
     * @since 0.06
     */
    ChannelFuture sendBinary(byte[] binary);
}