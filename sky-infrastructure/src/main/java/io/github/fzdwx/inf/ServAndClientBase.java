package io.github.fzdwx.inf;

import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.NonNull;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/22 15:55
 */
public abstract class ServAndClientBase<Type> {

    protected LoggingHandler logging;

    public void bind(Hooks<ChannelFuture> h) {
        h.call(bind());
    }

    public abstract int port();

    public abstract ChannelFuture bind(InetSocketAddress address);

    public void bind(InetSocketAddress address, Hooks<ChannelFuture> h) {
        h.call(bind(address));
    }

    public ChannelFuture bind() {
        return bind(new InetSocketAddress(port()));
    }

    public abstract void stop();

    public abstract Channel channel();

    @NonNull
    public final ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                if (logging != null) {
                    ch.pipeline().addLast(logging);
                }
                registerInitChannel().call(ch);
            }
        };
    }

    public abstract Hooks<SocketChannel> registerInitChannel();

    public abstract Class<? extends Channel> channelClassType();

    protected abstract Type me();

    public Type log(LogLevel level) {
        logging = new LoggingHandler(level);
        return me();
    }

    public Type log(LogLevel level, ByteBufFormat format) {
        logging = new LoggingHandler(level, format);
        return me();
    }

}