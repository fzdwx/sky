package io.github.fzdwx.inf.core;

import io.github.fzdwx.inf.Netty;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundHandler;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/22 21:55
 */
public interface Connection extends DisposableChannel {

    static Connection from(Channel channel) {
        Netty.requireNonNull(channel, "channel");

        if (channel.hasAttr(Netty.CONNECTION)) {
            return channel.attr(Netty.CONNECTION)
                    .get();
        }

        return new ConnectionImpl(channel);
    }

    /**
     * bind this on the channel.
     *
     * @see Connection#from(Channel)
     */
    default Connection bind() {
        channel().attr(Netty.CONNECTION)
                .set(this);
        return this;
    }

    /**
     * add handler to channel pipeline
     */
    default Connection addHandler(ChannelHandler handler) {
        if (handler instanceof ChannelOutboundHandler) {
            addHandlerFirst(handler);
        } else {
            addHandlerLast(handler);
        }
        return this;
    }

    default NettyInbound inbound() {
        return NettyInbound.unavailableInbound(this);
    }


    /**
     * add handler to channel pipeline first
     */
    default Connection addHandlerFirst(ChannelHandler handler) {
        channel().pipeline().addFirst(handler);
        return this;
    }

    /**
     * add handler to channel pipeline last
     */
    default Connection addHandlerLast(ChannelHandler handler) {
        channel().pipeline().addLast(handler);
        return this;
    }

    /**
     * Return false if it will force a close on terminal protocol events thus defeating
     * any pooling strategy
     * Return true (default) if it will release on terminal protocol events thus
     * keeping alive the channel if possible.
     *
     * @return whether or not the underlying {@link Connection} will be disposed on
     * terminal handler event
     */
    default boolean isPersistent() {
        return !channel().hasAttr(Netty.PERSISTENT_CHANNEL) ||
               channel().attr(Netty.PERSISTENT_CHANNEL).get();
    }

    /**
     * Mark the underlying channel as persistent or not.
     * If false, it will force a close on terminal protocol events thus defeating
     * any pooling strategy
     * if true (default), it will release on terminal protocol events thus
     * keeping alive the channel if possible.
     *
     * @param persist the boolean flag to mark the {@link Channel} as fully disposable
     * or reusable when a user handler has terminated
     *
     * @return this Connection
     */
    default Connection markPersistent(boolean persist) {
        if (persist && !channel().hasAttr(Netty.PERSISTENT_CHANNEL)) {
            return this;
        }
        else {
            channel().attr(Netty.PERSISTENT_CHANNEL)
                    .set(persist);
        }
        return this;
    }

    static final class ConnectionImpl extends AtomicLong implements Connection {

        private final Channel channel;

        public ConnectionImpl(final Channel channel) {
            this.channel = channel;
        }

        @Override
        public Channel channel() {
            return channel;
        }

        @Override
        public String toString() {
            return "simple connection {" + "channel=" + channel + '}';
        }
    }
}