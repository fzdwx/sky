package org.atomicode.inf.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundHandler;
import org.atomicode.inf.Netty;

import java.net.InetSocketAddress;

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

        return null;
    }

    /**
     * bind this on the channel.
     *
     * @see Connection#from(Channel)
     */
    default ChannelFuture bind(InetSocketAddress address) {
        channel().attr(Netty.CONNECTION)
                .set(this);
        return null;
    }

    default Connection addHandler(ChannelHandler handler) {
        return addHandler(handler.getClass().getSimpleName(), handler);
    }

    /**
     * add handler to channel pipeline
     */
    default Connection addHandler(String name, ChannelHandler handler) {
        if (handler instanceof ChannelOutboundHandler) {
            addHandlerFirst(name, handler);
        } else {
            addHandlerLast(name, handler);
        }
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
     *                or reusable when a user handler has terminated
     * @return this Connection
     */
    default Connection markPersistent(boolean persist) {
        if (persist && !channel().hasAttr(Netty.PERSISTENT_CHANNEL)) {
            return this;
        } else {
            channel().attr(Netty.PERSISTENT_CHANNEL)
                    .set(persist);
        }
        return this;
    }

    /**
     * Assign a {@link Runnable} to be invoked when reads have become idle for the given
     * timeout. This replaces any previously set idle callback.
     */
    default Connection onReadIdle(long idleTimeout, Runnable onReadIdle) {
        return removeHandler(Netty.readIdleHandlerName)
                .addHandlerFirst(Netty.readIdleHandlerName,
                        new Netty.InboundIdleStateHandler(idleTimeout, onReadIdle));
    }


    /**
     * Assign a {@link Runnable} to be invoked when writes have become idle for the given
     * timeout. This replaces any previously set idle callback.
     */
    default Connection onWriteIdle(long idleTimeout, Runnable onWriteIdle) {
        return removeHandler(Netty.writeIdleHandlerName)
                .addHandlerFirst(Netty.writeIdleHandlerName,
                        new Netty.OutboundIdleStateHandler(idleTimeout, onWriteIdle));
    }

    /**
     * add handler to channel pipeline first
     */
    default Connection addHandlerFirst(ChannelHandler handler) {
        channel().pipeline().addFirst(handler);
        return this;
    }

    default Connection addHandlerFirst(String handlerName, ChannelHandler handler) {
        channel().pipeline().addFirst(handlerName, handler);
        return this;
    }

    /**
     * add handler to channel pipeline last
     */
    default Connection addHandlerLast(ChannelHandler handler) {
        channel().pipeline().addLast(handler);
        return this;
    }

    default Connection addHandlerLast(String handlerName, ChannelHandler handler) {
        channel().pipeline().addLast(handlerName, handler);
        return this;
    }

    default ChannelFuture onTerminate() {
        return onDispose();
    }

    @Override
    default Connection onDispose(Disposable onDispose) {
        DisposableChannel.super.onDispose(onDispose);
        return this;
    }

    /**
     * Remove a named handler if present and return this context
     *
     * @return this Connection
     */
    default Connection removeHandler(String handlerName) {
        Netty.removeHandler(channel(), handlerName);
        return this;
    }

    /**
     * Replace a named handler if present and return this context.
     * If handler wasn't present, an {@link RuntimeException} will be thrown.
     * <p>
     * Note: if the new handler is of different type, dependent handling like
     * the "extractor" introduced via HTTP-based {@link #addHandler} might not
     * expect/support the new messages type.
     *
     * @return this Connection
     */
    default Connection replaceHandler(String handlerName, ChannelHandler handler) {
        Netty.replaceHandler(channel(), handlerName, handler);
        return this;
    }
}