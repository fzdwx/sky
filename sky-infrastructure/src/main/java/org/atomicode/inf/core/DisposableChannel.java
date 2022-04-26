package org.atomicode.inf.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.unix.DomainDatagramChannel;
import org.atomicode.inf.Netty;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/22 21:55
 */
public interface DisposableChannel extends Disposable {

    /**
     * return the underlying channel
     */
    Channel channel();

    /**
     * if server, return the local address
     * if client, return the remote address
     */
    default SocketAddress address() {
        Channel c = channel();
        if (c instanceof DatagramChannel || c instanceof DomainDatagramChannel) {
            SocketAddress a = c.remoteAddress();
            return a != null ? a : c.localAddress();
        }

        return c.remoteAddress();
    }

    default void dispose(Duration amount) {
        if (isDisposed()) {
            return;
        }

        Netty.requireNonNull(amount, "amount");

        dispose();

        try {
            channel().closeFuture().get(amount.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default void dispose() {
        if (channel().isOpen()) {
            channel().close();
        }
    }

    @Override
    default boolean isDisposed() {
        return !channel().isActive();
    }

    default ChannelFuture onDispose() {
        return channel().closeFuture();
    }

    default DisposableChannel onDispose(Disposable onDispose) {
        requireNonNull(onDispose, "onDispose");
        onDispose().addListener(f -> onDispose.dispose());
        return this;
    }
}