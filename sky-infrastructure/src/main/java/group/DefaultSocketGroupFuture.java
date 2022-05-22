package group;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.BlockingOperationException;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import socket.Socket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class DefaultSocketGroupFuture<K> extends DefaultPromise<Void> implements SocketGroupFuture<K> {

    private final SocketGroup<K> group;
    private final Map<Socket, ChannelFuture> futures;
    private int successCount;
    private int failureCount;

    private final ChannelFutureListener childListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            boolean success = future.isSuccess();
            boolean callSetDone;
            synchronized (DefaultSocketGroupFuture.this) {
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }

                callSetDone = successCount + failureCount == futures.size();
                assert successCount + failureCount <= futures.size();
            }

            if (callSetDone) {
                if (failureCount > 0) {
                    List<Map.Entry<Socket, Throwable>> failed =
                            new ArrayList<>(failureCount);
                    for (ChannelFuture f : futures.values()) {
                        if (!f.isSuccess()) {
                            failed.add(new DefaultEntry<>(Socket.create(f.channel()), f.cause()));
                        }
                    }
                    setFailure0(new ChannelGroupException(failed));
                } else {
                    setSuccess0();
                }
            }
        }
    };

    /**
     * <
     * Creates a new instance.
     */
    DefaultSocketGroupFuture(SocketGroup<K> group, Collection<ChannelFuture> futures, EventExecutor executor) {
        super(executor);
        if (group == null) {
            throw new NullPointerException("group");
        }
        if (futures == null) {
            throw new NullPointerException("futures");
        }

        this.group = group;

        Map<Socket, ChannelFuture> futureMap = new LinkedHashMap<>();
        for (ChannelFuture f : futures) {
            futureMap.put(Socket.create(f.channel()), f);
        }

        this.futures = Collections.unmodifiableMap(futureMap);

        for (ChannelFuture f : this.futures.values()) {
            f.addListener(childListener);
        }

        // Done on arrival?
        if (this.futures.isEmpty()) {
            setSuccess0();
        }
    }

    DefaultSocketGroupFuture(SocketGroup<K> group, Map<Socket, ChannelFuture> futures, EventExecutor executor) {
        super(executor);
        this.group = group;
        this.futures = Collections.unmodifiableMap(futures);
        for (ChannelFuture f : this.futures.values()) {
            f.addListener(childListener);
        }

        // Done on arrival?
        if (this.futures.isEmpty()) {
            setSuccess0();
        }
    }

    @Override
    public SocketGroup<K> group() {
        return group;
    }

    @Override
    public ChannelFuture find(Socket channel) {
        return futures.get(channel);
    }

    @Override
    public synchronized boolean isPartialSuccess() {
        return successCount != 0 && successCount != futures.size();
    }

    @Override
    public synchronized boolean isPartialFailure() {
        return failureCount != 0 && failureCount != futures.size();
    }

    @Override
    public Iterator<ChannelFuture> iterator() {
        return futures.values().iterator();
    }

    @Override
    public void forEach(final Consumer<? super ChannelFuture> action) {
        SocketGroupFuture.super.forEach(action);
    }

    @Override
    public DefaultSocketGroupFuture<K> setSuccess(Void result) {
        throw new IllegalStateException();
    }

    @Override
    public boolean trySuccess(Void result) {
        throw new IllegalStateException();
    }

    @Override
    public DefaultSocketGroupFuture<K> setFailure(Throwable cause) {
        throw new IllegalStateException();
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        throw new IllegalStateException();
    }

    @Override
    public ChannelGroupException cause() {
        return (ChannelGroupException) super.cause();
    }

    @Override
    public DefaultSocketGroupFuture<K> addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    @Override
    public DefaultSocketGroupFuture<K> addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.addListeners(listeners);
        return this;
    }

    @Override
    public DefaultSocketGroupFuture<K> removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    @Override
    public DefaultSocketGroupFuture<K> removeListeners(
            GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.removeListeners(listeners);
        return this;
    }

    @Override
    public DefaultSocketGroupFuture<K> await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public DefaultSocketGroupFuture<K> awaitUninterruptibly() {
        super.awaitUninterruptibly();
        return this;
    }

    @Override
    public DefaultSocketGroupFuture<K> sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public DefaultSocketGroupFuture<K> syncUninterruptibly() {
        super.syncUninterruptibly();
        return this;
    }

    @Override
    protected void checkDeadLock() {
        EventExecutor e = executor();
        if (e != null && e != ImmediateEventExecutor.INSTANCE && e.inEventLoop()) {
            throw new BlockingOperationException();
        }
    }

    private void setSuccess0() {
        super.setSuccess(null);
    }

    private void setFailure0(ChannelGroupException cause) {
        super.setFailure(cause);
    }

    private static final class DefaultEntry<K, V> implements Map.Entry<K, V> {

        private final K key;
        private final V value;

        DefaultEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("read-only");
        }
    }
}