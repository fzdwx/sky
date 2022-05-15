package core;

import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import serializer.JsonSerializer;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 14:34
 */
public interface Transport<IMPL> {

    default IMPL listen(int port) {
        return listen(new InetSocketAddress(port));
    }

    /**
     * start transport
     */
    IMPL listen(InetSocketAddress address);

    default IMPL listen(String host, int port) {
        return listen(new InetSocketAddress(host, port));
    }

    IMPL afterListen(Hooks<ChannelFuture> hooks);

    IMPL onSuccess(Hooks<IMPL> hooks);

    IMPL onFailure(Hooks<Throwable> hooks);

    IMPL withSerializer(JsonSerializer serializer);

    IMPL withInitChannel(Hooks<SocketChannel> hooks);

    ChannelInitializer<SocketChannel> channelInitializer();

    ChannelFuture dispose();

    void close();

    boolean sslFlag();

    JsonSerializer serializer();

    /**
     * @return this
     */
    IMPL impl();

    default IMPL withWorker(int workerCount) {
        return withWorker(new NioEventLoopGroup(workerCount));
    }

    IMPL withWorker(EventLoopGroup worker);

    /**
     * set child log handler
     */
    default IMPL withLog(LogLevel level) {
        return withLog(new LoggingHandler(level));
    }

    IMPL withLog(LoggingHandler loggingHandler);
}