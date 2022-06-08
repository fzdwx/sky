package core.socket;

import core.socket.inter.SocketImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;

/**
 * session.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 13:11
 * @since 0.06
 */
public interface Socket {

    Channel channel();

    static Socket create(Channel channel) {
        return new SocketImpl(channel);
    }

    /**
     * close session.
     *
     * @since 0.07
     */
    ChannelFuture reject();

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
     * set attr to value
     */
    default <T> Socket attr(String key, T value) {
        channel().attr(AttributeKey.<T>valueOf(key)).set(value);
        return this;
    }

    /**
     * set attr to value
     */
    default <T> Socket attr(AttributeKey<T> key, T value) {
        channel().attr(key).set(value);
        return this;
    }

    /**
     * get attr
     */
    default <T> T attr(String key) {
        return channel().attr(AttributeKey.<T>valueOf(key)).get();
    }

    /**
     * get attr
     */
    default <T> T attr(AttributeKey<T> key) {
        return channel().attr(key).get();
    }

    /**
     * has Attr?
     */
    default boolean hasAttr(String key) {
        return channel().hasAttr(AttributeKey.valueOf(key));
    }

    /**
     * has Attr?
     */
    default boolean hasAttr(AttributeKey<?> key) {
        return channel().hasAttr(key);
    }
}