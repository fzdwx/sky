package core;

import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import core.serializer.JsonSerializer;

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

    IMPL jsonSerializer(JsonSerializer serializer);

    IMPL addSocketChannelHooks(Hooks<SocketChannel> hooks);

    ChannelInitializer<SocketChannel> workerHandler();

    ChannelFuture dispose();

    void shutdown();

    ChannelFuture close();

    boolean ssl();

    JsonSerializer jsonSerializer();

    IMPL impl();

    IMPL worker(int workerCount);

    /**
     * set child log handler
     */
    default IMPL log(LogLevel level) {
        return log(new LoggingHandler(level));
    }

    IMPL log(LoggingHandler loggingHandler);

    default void callStartFuture(ChannelFuture startFuture,
                                 Hooks<ChannelFuture> afterListen,
                                 Hooks<Server> onSuccessHooks,
                                 Hooks<Throwable> onFailureHooks
    ) {

    }
}