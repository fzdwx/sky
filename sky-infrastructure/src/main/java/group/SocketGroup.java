package group;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import socket.Socket;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/22 8:16
 * @see io.netty.channel.group.ChannelGroup
 * @see Socket
 */
public interface SocketGroup<K> extends Comparable<SocketGroup<K>> {


    /**
     * Returns the name of this group.  A group name is purely for helping
     * you to distinguish one group from others.
     */
    String name();

    /**
     * Returns the {@link Channel} which has the specified {@code Key}.
     *
     * @return the matching {@link Channel} if found. {@code null} otherwise.
     */
    Socket find(K key);

    boolean add(K key, Socket socket);

    /**
     * Writes the specified {@code message} to all {@link Channel}s in this
     * group. If the specified {@code message} is an instance of
     * {@link ByteBuf}, it is automatically
     * {@linkplain ByteBuf#duplicate() duplicated} to avoid a race
     * condition. The same is true for {@link ByteBufHolder}. Please note that this operation is asynchronous as
     * {@link Channel#write(Object)} is.
     *
     * @return itself
     */
    SocketGroupFuture<K> write(Object message);

    /**
     * Writes the specified {@code message} to all {@link Channel}s in this
     * group that are matched by the given {@link SocketMatcher}. If the specified {@code message} is an instance of
     * {@link ByteBuf}, it is automatically
     * {@linkplain ByteBuf#duplicate() duplicated} to avoid a race
     * condition. The same is true for {@link ByteBufHolder}. Please note that this operation is asynchronous as
     * {@link Channel#write(Object)} is.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroupFuture<K> write(Object message, SocketMatcher matcher);

    /**
     * Writes the specified {@code message} to all {@link Channel}s in this
     * group that are matched by the given {@link SocketMatcher}. If the specified {@code message} is an instance of
     * {@link ByteBuf}, it is automatically
     * {@linkplain ByteBuf#duplicate() duplicated} to avoid a race
     * condition. The same is true for {@link ByteBufHolder}. Please note that this operation is asynchronous as
     * {@link Channel#write(Object)} is.
     * <p>
     * If {@code voidPromise} is {@code true} {@link Channel#voidPromise()} is used for the writes and so the same
     * restrictions to the returned {@link SocketGroupFuture <K>} apply as to a void promise.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroupFuture<K> write(Object message, SocketMatcher matcher, boolean voidPromise);

    /**
     * Flush all {@link Channel}s in this
     * group. If the specified {@code messages} are an instance of
     * {@link ByteBuf}, it is automatically
     * {@linkplain ByteBuf#duplicate() duplicated} to avoid a race
     * condition. Please note that this operation is asynchronous as
     * {@link Channel#write(Object)} is.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroup<K> flush();

    /**
     * Flush all {@link Channel}s in this group that are matched by the given {@link SocketMatcher}.
     * If the specified {@code messages} are an instance of
     * {@link ByteBuf}, it is automatically
     * {@linkplain ByteBuf#duplicate() duplicated} to avoid a race
     * condition. Please note that this operation is asynchronous as
     * {@link Channel#write(Object)} is.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroup<K> flush(SocketMatcher matcher);

    /**
     * Shortcut for calling {@link #write(Object)} and {@link #flush()}.
     */
    SocketGroupFuture<K> writeAndFlush(Object message);

    /**
     * Shortcut for calling {@link #write(Object)} and {@link #flush()} and only act on
     * {@link Channel}s that are matched by the {@link SocketMatcher}.
     */
    SocketGroupFuture<K> writeAndFlush(Object message, SocketMatcher matcher);

    /**
     * Shortcut for calling {@link #write(Object, SocketMatcher, boolean)} and {@link #flush()} and only act on
     * {@link Channel}s that are matched by the {@link SocketMatcher}.
     */
    SocketGroupFuture<K> writeAndFlush(Object message, SocketMatcher matcher, boolean voidPromise);

    /**
     * Disconnects all {@link Channel}s in this group from their remote peers.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroupFuture<K> disconnect();

    /**
     * Disconnects all {@link Channel}s in this group from their remote peers,
     * that are matched by the given {@link SocketMatcher}.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroupFuture<K> disconnect(SocketMatcher matcher);

    /**
     * Closes all {@link Channel}s in this group.  If the {@link Channel} is
     * connected to a remote peer or bound to a local address, it is
     * automatically disconnected and unbound.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroupFuture<K> close();

    /**
     * Closes all {@link Channel}s in this group that are matched by the given {@link SocketMatcher}.
     * If the {@link Channel} is  connected to a remote peer or bound to a local address, it is
     * automatically disconnected and unbound.
     *
     * @return the {@link SocketGroupFuture <K>} instance that notifies when
     * the operation is done for all channels
     */
    SocketGroupFuture<K> close(SocketMatcher matcher);

    /**
     * Returns the {@link SocketGroupFuture <K>} which will be notified when all {@link Channel}s that are part of this
     * {@link io.netty.channel.group.ChannelGroup}, at the time of calling, are closed.
     */
    SocketGroupFuture<K> newCloseFuture();

    /**
     * Returns the {@link SocketGroupFuture <K>} which will be notified when all {@link Channel}s that are part of this
     * {@link io.netty.channel.group.ChannelGroup}, at the time of calling, are closed.
     */
    SocketGroupFuture<K> newCloseFuture(SocketMatcher matcher);

    int size();

    boolean isEmpty();

    void clear();
}