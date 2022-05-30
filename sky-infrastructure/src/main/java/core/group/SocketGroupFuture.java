package core.group;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import core.socket.Socket;

import java.util.Iterator;

/**
 * copy from io.netty.channel.group.ChannelGroupFuture
 */
public interface SocketGroupFuture<K> extends Future<Void>, Iterable<ChannelFuture> {

    /**
     * Returns the {@link SocketGroup} which is associated with this future.
     */
    SocketGroup<K> group();

    /**
     * Returns the {@link ChannelFuture} of the individual I/O operation which
     * is associated with the specified {@link  Socket}.
     *
     * @return the matching {@link ChannelFuture} if found.
     * {@code null} otherwise.
     */
    ChannelFuture find(Socket s);

    /**
     * Returns {@code true} if and only if all I/O operations associated with
     * this future were successful without any failure.
     */
    @Override
    boolean isSuccess();

    @Override
    SocketGroupException cause();

    @Override
    SocketGroupFuture<K> addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    SocketGroupFuture<K> addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    SocketGroupFuture<K> removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    SocketGroupFuture<K> removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners);

    @Override
    SocketGroupFuture<K> sync() throws InterruptedException;

    @Override
    SocketGroupFuture<K> syncUninterruptibly();

    @Override
    SocketGroupFuture<K> await() throws InterruptedException;

    @Override
    SocketGroupFuture<K> awaitUninterruptibly();

    /**
     * Returns {@code true} if and only if the I/O operations associated with
     * this future were partially successful with some failure.
     */
    boolean isPartialSuccess();

    /**
     * Returns {@code true} if and only if the I/O operations associated with
     * this future have failed partially with some success.
     */
    boolean isPartialFailure();

    /**
     * Returns the {@link Iterator} that enumerates all {@link ChannelFuture}s
     * which are associated with this future.  Please note that the returned
     * {@link Iterator} is is unmodifiable, which means a {@link ChannelFuture}
     * cannot be removed from this future.
     */
    @Override
    Iterator<ChannelFuture> iterator();
}