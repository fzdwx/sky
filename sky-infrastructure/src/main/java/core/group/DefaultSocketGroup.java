package core.group;

import core.socket.Socket;
import io.github.fzdwx.lambada.anno.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ServerChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/22 8:25
 */
public class DefaultSocketGroup<K> implements SocketGroup<K> {

    private static final AtomicInteger nextId = new AtomicInteger();
    private final String name;
    private final EventExecutor executor;
    private final ConcurrentMap<K, Socket> serverChannels = PlatformDependent.newConcurrentHashMap();
    private final ConcurrentMap<K, Socket> nonServerChannels = PlatformDependent.newConcurrentHashMap();
    private final VoidSocketGroupFuture<K> voidFuture = new VoidSocketGroupFuture<>(this);
    private final boolean stayClosed;
    private volatile boolean closed;

    public DefaultSocketGroup(EventExecutor executor) {
        this(executor, false);
    }

    /**
     * Creates a new group with the specified {@code name} and {@link EventExecutor} to notify the
     * {@link SocketGroupFuture}s.  Please note that different groups can have the same name, which means no
     * duplicate check is done against group names.
     */
    public DefaultSocketGroup(String name, EventExecutor executor) {
        this(name, executor, false);
    }

    /**
     * Creates a new group with a generated name and the provided {@link EventExecutor} to notify the
     * {@link SocketGroupFuture}s. {@code stayClosed} defines whether or not, this group can be closed
     * more than once. Adding channels to a closed group will immediately close them, too. This makes it
     * easy, to shutdown server and child channels at once.
     */
    public DefaultSocketGroup(EventExecutor executor, boolean stayClosed) {
        this("group-0x" + Integer.toHexString(nextId.incrementAndGet()), executor, stayClosed);
    }

    /**
     * Creates a new group with the specified {@code name} and {@link EventExecutor} to notify the
     * {@link SocketGroupFuture}s. {@code stayClosed} defines whether or not, this group can be closed
     * more than once. Adding channels to a closed group will immediately close them, too. This makes it
     * easy, to shutdown server and child channels at once. Please note that different groups can have
     * the same name, which means no duplicate check is done against group names.
     */
    public DefaultSocketGroup(String name, EventExecutor executor, boolean stayClosed) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;
        this.executor = executor;
        this.stayClosed = stayClosed;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Socket find(K id) {
        Socket c = nonServerChannels.get(id);
        if (c != null) {
            return c;
        } else {
            return serverChannels.get(id);
        }
    }

    @Override
    public boolean add(K key, Socket socket) {
        ConcurrentMap<K, Socket> map =
                socket.channel() instanceof ServerChannel ? serverChannels : nonServerChannels;

        boolean added = map.putIfAbsent(key, socket) == null;
        if (added) {
            socket.channel().closeFuture().addListener(remover(key));
        }

        if (stayClosed && closed) {

            // First add channel, than check if closed.
            // Seems inefficient at first, but this way a volatile
            // gives us enough synchronization to be thread-safe.
            //
            // If true: Close right away.
            // (Might be closed a second time by ChannelGroup.close(), but this is ok)
            //
            // If false: Channel will definitely be closed by the ChannelGroup.
            // (Because closed=true always happens-before ChannelGroup.close())
            //
            // See https://github.com/netty/netty/issues/4020
            socket.channel().close();
        }

        return added;
    }

    @Override
    public SocketGroupFuture<K> write(Object message) {
        return write(message, SocketMatcher.ALL);
    }

    @Override
    public SocketGroupFuture<K> write(Object message, SocketMatcher matcher) {
        return write(message, matcher, false);
    }

    @Override
    public SocketGroupFuture<K> write(Object message, SocketMatcher matcher, boolean voidPromise) {
        if (message == null) {
            throw new NullPointerException("message");
        }
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }

        final SocketGroupFuture<K> future;
        if (voidPromise) {
            for (Socket c : nonServerChannels.values()) {
                if (matcher.matches(c)) {
                    c.channel().write(safeDuplicate(message), c.channel().voidPromise());
                }
            }
            future = voidFuture;
        } else {
            Map<Socket, ChannelFuture> futures = new LinkedHashMap<>(size());
            for (Socket c : nonServerChannels.values()) {
                if (matcher.matches(c)) {
                    futures.put(c, c.channel().write(safeDuplicate(message)));
                }
            }
            future = new DefaultSocketGroupFuture<K>(this, futures, executor);
        }
        ReferenceCountUtil.release(message);
        return future;
    }

    @Override
    public SocketGroup<K> flush() {
        return flush(SocketMatcher.ALL);
    }

    @Override
    public SocketGroup<K> flush(SocketMatcher matcher) {
        for (Socket socket : nonServerChannels.values()) {
            if (matcher.matches(socket)) {
                socket.channel().flush();
            }
        }
        return this;
    }

    @Override
    public SocketGroupFuture<K> writeAndFlush(Object message) {
        return writeAndFlush(message, SocketMatcher.ALL);
    }

    @Override
    public SocketGroupFuture<K> writeAndFlush(Object message, SocketMatcher matcher) {
        return writeAndFlush(message, matcher, false);
    }

    @Override
    public SocketGroupFuture<K> writeAndFlush(Object message, SocketMatcher matcher, boolean voidPromise) {
        if (message == null) {
            throw new NullPointerException("message");
        }

        final SocketGroupFuture<K> future;
        if (voidPromise) {
            for (Socket s : nonServerChannels.values()) {
                if (matcher.matches(s)) {
                    s.channel().writeAndFlush(safeDuplicate(message), s.channel().voidPromise());
                }
            }
            future = voidFuture;
        } else {
            Map<Socket, ChannelFuture> futures = new LinkedHashMap<>(size());
            for (Socket s : nonServerChannels.values()) {
                if (matcher.matches(s)) {
                    futures.put(s, s.channel().writeAndFlush(safeDuplicate(message)));
                }
            }
            future = new DefaultSocketGroupFuture<K>(this, futures, executor);
        }
        ReferenceCountUtil.release(message);
        return future;
    }

    @Override
    public SocketGroupFuture<K> disconnect() {
        return disconnect(SocketMatcher.ALL);
    }

    @Override
    public SocketGroupFuture<K> disconnect(SocketMatcher matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }

        Map<Socket, ChannelFuture> futures =
                new LinkedHashMap<>(size());

        for (Socket s : serverChannels.values()) {
            if (matcher.matches(s)) {
                futures.put(s, s.channel().disconnect());
            }
        }
        for (Socket s : nonServerChannels.values()) {
            if (matcher.matches(s)) {
                futures.put(s, s.channel().disconnect());
            }
        }

        return new DefaultSocketGroupFuture<>(this, futures, executor);
    }

    @Override
    public SocketGroupFuture<K> close() {
        return close(SocketMatcher.ALL);
    }

    @Override
    public SocketGroupFuture<K> close(SocketMatcher matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }

        Map<Socket, ChannelFuture> futures =
                new LinkedHashMap<>(size());

        if (stayClosed) {
            // It is important to set the closed to true, before closing channels.
            // Our invariants are:
            // closed=true happens-before ChannelGroup.close()
            // ChannelGroup.add() happens-before checking closed==true
            //
            // See https://github.com/netty/netty/issues/4020
            closed = true;
        }

        for (Socket s : serverChannels.values()) {
            if (matcher.matches(s)) {
                futures.put(s, s.channel().close());
            }
        }
        for (Socket s : nonServerChannels.values()) {
            if (matcher.matches(s)) {
                futures.put(s, s.channel().close());
            }
        }

        return new DefaultSocketGroupFuture<K>(this, futures, executor);
    }

    @Override
    public SocketGroupFuture<K> newCloseFuture() {
        return newCloseFuture(SocketMatcher.ALL);
    }

    @Override
    public SocketGroupFuture<K> newCloseFuture(SocketMatcher matcher) {
        Map<Socket, ChannelFuture> futures = new LinkedHashMap<>(size());

        for (Socket s : serverChannels.values()) {
            if (matcher.matches(s)) {
                futures.put(s, s.channel().closeFuture());
            }
        }
        for (Socket s : nonServerChannels.values()) {
            if (matcher.matches(s)) {
                futures.put(s, s.channel().closeFuture());
            }
        }

        return new DefaultSocketGroupFuture<K>(this, futures, executor);
    }

    @Override
    public int size() {
        return nonServerChannels.size() + serverChannels.size();
    }

    @Override
    public boolean isEmpty() {
        return nonServerChannels.isEmpty() && serverChannels.isEmpty();
    }

    @Override
    public void clear() {
        nonServerChannels.clear();
        serverChannels.clear();
    }

    public boolean remove(K key) {
        Socket s;

        s = nonServerChannels.remove(key);
        if (s == null) {
            s = serverChannels.remove(key);
        }

        if (s == null) {
            return false;
        }

        s.channel().closeFuture().removeListener(remover(key));
        return true;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(name: " + name() + ", size: " + size() + ')';
    }

    @Override
    public int compareTo(SocketGroup o) {
        int v = name().compareTo(o.name());
        if (v != 0) {
            return v;
        }

        return System.identityHashCode(this) - System.identityHashCode(o);
    }

    ChannelFutureListener remover(K key) {
        return new Remover(key);
    }

    // Create a safe duplicate of the message to write it to a channel but not affect other writes.
    // See https://github.com/netty/netty/issues/1461
    private static Object safeDuplicate(Object message) {
        if (message instanceof ByteBuf) {
            return ((ByteBuf) message).retainedDuplicate();
        } else if (message instanceof ByteBufHolder) {
            return ((ByteBufHolder) message).retainedDuplicate();
        } else {
            return ReferenceCountUtil.retain(message);
        }
    }

    @RequiredArgsConstructor
    class Remover implements ChannelFutureListener {

        private final K key;

        @Override
        public void operationComplete(@NotNull final ChannelFuture future) throws Exception {
            remove(key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Remover remover = (Remover) o;
            return Objects.equals(key, remover.key);
        }
    }


}