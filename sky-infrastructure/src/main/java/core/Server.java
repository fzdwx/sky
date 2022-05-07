package core;

import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 14:35
 */
public class Server implements Transport<Server> {

    private Hooks<SocketChannel> socketChannelInitHooks;

    private Hooks<ChannelFuture> afterStartHooks;

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private ServerBootstrap bootstrap;

    private boolean sslFlag;

    private ChannelFuture startFuture;

    private LoggingHandler loggingHandler;

    private SslHandler sslHandler;

    private Class<? extends ServerChannel> channelType = NioServerSocketChannel.class;

    private final Map<ChannelOption<?>, Object> serverOptions = new HashMap<>();

    private final Map<ChannelOption<?>, Object> childOptions = new HashMap<>();

    public Server() {
        this.bootstrap = new ServerBootstrap();
    }

    @Override
    public Server bind(final InetSocketAddress address) {
        preBind();

        for (Map.Entry<ChannelOption<?>, ?> entry : serverOptions.entrySet()) {
            this.bootstrap = bootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.bootstrap = bootstrap.childOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        startFuture = this.bootstrap
                .childHandler(channelInitializer())
                .channel(NioServerSocketChannel.class)
                .group(boss, worker)
                .bind(address).syncUninterruptibly();

        if (afterStartHooks != null) {
            afterStartHooks.call(startFuture);
        }

        return this;
    }

    public Server afterStart(Hooks<ChannelFuture> hooks) {
        this.afterStartHooks = hooks;
        return this;
    }

    /**
     * @apiNote before bind
     */
    public Server withGroup(final int bossCount, final int workerCount) {
        this.boss = new NioEventLoopGroup(bossCount);
        this.worker = new NioEventLoopGroup(workerCount);
        return this;
    }

    /**
     * @apiNote before bind
     */
    public Server withGroup(final EventLoopGroup boss, final EventLoopGroup worker) {
        this.boss = boss;
        this.worker = worker;
        return this;
    }

    /**
     * @apiNote before bind
     */
    public Server withBossWorkerGroup(final EventLoopGroup boss) {
        this.worker = boss;
        return this;
    }

    /**
     * @apiNote default is {@link NioServerSocketChannel}
     */
    public Server withChannelType(Class<? extends ServerChannel> channelType) {
        this.channelType = channelType;
        return this;
    }

    /**
     * @apiNote before bind
     */
    @Override
    public Server withWorkerGroup(final EventLoopGroup worker) {
        this.worker = worker;
        return this;
    }

    /**
     * @apiNote before bind
     */
    public <T> Server withServerOptions(ChannelOption<T> option, T t) {
        serverOptions.put(option, t);
        return this;
    }

    /**
     * @apiNote before bind
     */
    public <T> Server withChildOptions(ChannelOption<T> option, T t) {
        childOptions.put(option, t);
        return this;
    }

    @Override
    public void dispose() {
        if (!this.worker.isShutdown()) {
            this.worker.shutdownGracefully();
        }
        if (!this.boss.isShutdown()) {
            this.boss.shutdownGracefully();
        }
    }

    @Override
    public boolean sslFlag() {
        return this.sslFlag;
    }

    @Override
    public Server impl() {
        return this;
    }

    /**
     * @apiNote before bind
     */
    @Override
    public ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                if (loggingHandler != null) {
                    ch.pipeline().addLast(loggingHandler);
                }
                if (sslHandler != null) {
                    ch.pipeline().addLast(sslHandler);
                }
                socketChannelInitHooks.call(ch);
            }
        };
    }

    @Override
    public Server withLog(final LoggingHandler loggingHandler) {
        this.loggingHandler = loggingHandler;
        return this;
    }

    public Server withSsl(final SslHandler sslHandler) {
        this.sslHandler = sslHandler;
        this.sslFlag = true;
        return this;
    }

    @Override
    public Server withInitChannel(Hooks<SocketChannel> hooks) {
        socketChannelInitHooks = hooks;
        return this;
    }

    public Server withBossWorkerGroup(final int bossCount) {
        return withBossWorkerGroup(new NioEventLoopGroup(bossCount));
    }

    private void preBind() {
        Objects.requireNonNull(channelType, "channelType is null");
        Objects.requireNonNull(boss, "boss event group is null");
        Objects.requireNonNull(worker, "worker event group is null");
    }
}