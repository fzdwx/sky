package core;

import core.common.Disposer;
import core.serializer.JsonSerializer;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/6 14:34
 */
public interface Transport<IMPL> extends Disposer {

    default Disposer listen(int port) {
        return listen(new InetSocketAddress(port));
    }

    /**
     * start transport
     */
    Disposer listen(InetSocketAddress address);

    default Disposer listen(String host, int port) {
        return listen(new InetSocketAddress(host, port));
    }

    IMPL afterListen(Hooks<ChannelFuture> hooks);

    IMPL onSuccess(Hooks<IMPL> hooks);

    IMPL onFailure(Hooks<Throwable> hooks);

    IMPL onShutDown(Hooks<ChannelFutureListener> hooks);

    IMPL jsonSerializer(JsonSerializer serializer);

    IMPL childHandler(Hooks<SocketChannel> hooks);

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