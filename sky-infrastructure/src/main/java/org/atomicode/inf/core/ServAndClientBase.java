package org.atomicode.inf.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.NonNull;
import org.atomicode.fzdwx.lambada.fun.Hooks;
import org.atomicode.inf.Netty;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/22 15:55
 */
public abstract class ServAndClientBase<Type> implements Connection {

    protected EventLoopGroup workerGroup;
    protected LoggingHandler logging;
    protected String host;
    protected int port;
    protected int chunkSize = Netty.DEFAULT_CHUNK_SIZE;

    public Type host(String host) {
        this.host = host;

        return me();
    }

    public Type port(int port) {
        this.port = port;

        return this.me();
    }

    public Type chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;

        return this.me();
    }

    public int port() {
        return port;
    }

    public String host() {
        return this.host;
    }

    public abstract ChannelFuture bind(InetSocketAddress address);

    public ChannelFuture bind() {
        return bind(new InetSocketAddress(port()));
    }

    public ChannelFuture bind(Hooks<Type> h) {
        final var bindFuture = bind(new InetSocketAddress(host, port));
        bindFuture.addListener(f -> {
            h.call(me());
        });
        return bindFuture;
    }

    public abstract void stop();

    @NonNull
    public final ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                if (logging != null) {
                    ch.pipeline().addLast(logging);
                }
                mountInitChannel().call(ch);
            }
        };
    }

    public abstract Hooks<SocketChannel> mountInitChannel();

    public abstract Class<? extends Channel> channelClassType();

    public Type log(LogLevel level) {
        logging = new LoggingHandler(level);
        return me();
    }

    public Type log(LogLevel level, ByteBufFormat format) {
        logging = new LoggingHandler(level, format);
        return me();
    }

    protected abstract Type me();

}