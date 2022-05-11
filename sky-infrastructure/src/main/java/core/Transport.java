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

    /**
     * start transport
     */
    IMPL bind(InetSocketAddress address);

    IMPL withWorkerGroup(EventLoopGroup worker);

    IMPL withSerializer(JsonSerializer serializer);

    ChannelInitializer<SocketChannel> channelInitializer();

    IMPL withLog(LoggingHandler loggingHandler);

    IMPL withInitChannel(Hooks<SocketChannel> hooks);

    ChannelFuture dispose();

    void shutdown();

    boolean sslFlag();

    JsonSerializer serializer();

    /**
     * @return this
     */
    IMPL impl();


    default IMPL bind(int port) {
        return bind(new InetSocketAddress(port));
    }

    default IMPL bind(String host, int port) {
        return bind(new InetSocketAddress(host, port));
    }

    default IMPL withWorkerGroup(int workerCount) {
        return withWorkerGroup(new NioEventLoopGroup(workerCount));
    }

    /**
     * set child log handler
     */
    default IMPL withLog(LogLevel level) {
        return withLog(new LoggingHandler(level));
    }
}