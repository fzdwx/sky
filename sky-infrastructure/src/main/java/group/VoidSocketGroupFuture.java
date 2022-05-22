package group;

import io.github.fzdwx.lambada.Collections;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroupException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import socket.Socket;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * copy from netty
 */
public final class VoidSocketGroupFuture<K> implements SocketGroupFuture<K> {

    private static final Iterator<ChannelFuture> EMPTY = Collections.<ChannelFuture>emptyList().iterator();
    private final SocketGroup<K> group;

    VoidSocketGroupFuture(SocketGroup<K> group) {
        this.group = group;
    }

    @Override
    public SocketGroup<K> group() {
        return group;
    }

    @Override
    public ChannelFuture find(Socket c) {
        return null;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public ChannelGroupException cause() {
        return null;
    }

    @Override
    public boolean isPartialSuccess() {
        return false;
    }

    @Override
    public boolean isPartialFailure() {
        return false;
    }

    @Override
    public SocketGroupFuture<K> addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        throw reject();
    }

    @Override
    public SocketGroupFuture<K> addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        throw reject();
    }

    @Override
    public SocketGroupFuture<K> removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        throw reject();
    }

    @Override
    public SocketGroupFuture<K> removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        throw reject();
    }

    @Override
    public SocketGroupFuture<K> await() {
        throw reject();
    }

    @Override
    public SocketGroupFuture<K> awaitUninterruptibly() {
        throw reject();
    }

    @Override
    public SocketGroupFuture<K> syncUninterruptibly() {
        throw reject();
    }

    @Override
    public SocketGroupFuture<K> sync() {
        throw reject();
    }

    @Override
    public Iterator<ChannelFuture> iterator() {
        return EMPTY;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        throw reject();
    }

    @Override
    public boolean await(long timeoutMillis) {
        throw reject();
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        throw reject();
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        throw reject();
    }

    @Override
    public Void getNow() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @param mayInterruptIfRunning this value has no effect in this implementation.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Void get() {
        throw reject();
    }

    @Override
    public Void get(long timeout, TimeUnit unit)  {
        throw reject();
    }

    private static RuntimeException reject() {
        return new IllegalStateException("void future");
    }
}