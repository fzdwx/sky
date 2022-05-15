package core;

import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import serializer.JsonSerializer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 14:35
 */
public class Server implements Transport<Server> {

    private final Map<ChannelOption<?>, Object> serverOptions = new HashMap<>();
    private final Map<ChannelOption<?>, Object> childOptions = new HashMap<>();
    private final List<ChannelHandler> serverHandlers = new java.util.ArrayList<>();
    private Hooks<SocketChannel> socketChannelInitHooks;
    private Hooks<ChannelFuture> afterListen;
    private Hooks<Server> onSuccessHooks;
    private Hooks<Throwable> onFailureHooks;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap bootstrap;
    private boolean sslFlag;
    private ChannelFuture startFuture;
    private LoggingHandler loggingHandler;
    private SslHandler sslHandler;
    private InetSocketAddress address;
    private JsonSerializer serializer;
    private boolean startFlag = false;
    private Class<? extends ServerChannel> channelType = NioServerSocketChannel.class;

    public Server() {
        this.bootstrap = new ServerBootstrap();
    }

    @Override
    public Server listen(final InetSocketAddress address) {
        preStart(address);

        if (serverHandlers.size() > 0) {
            serverHandlers.forEach(bootstrap::handler);
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : serverOptions.entrySet()) {
            this.bootstrap = bootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.bootstrap = bootstrap.childOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        startFuture = this.bootstrap.childHandler(channelInitializer()).channel(channelType).group(boss, worker).bind(address).syncUninterruptibly();

        startFuture.addListener(f -> {
            if (afterListen != null) {
                afterListen.call(startFuture);
            }

            if (f.isSuccess()) {
                if (this.onSuccessHooks != null) {
                    this.onSuccessHooks.call(this);
                }
            } else {
                if (this.onFailureHooks != null) {
                    this.onFailureHooks.call(f.cause());
                }
            }
        });

        return this;
    }

    @Override
    public Server afterListen(Hooks<ChannelFuture> hooks) {
        checkStart();

        this.afterListen = hooks;
        return this;
    }

    @Override
    public Server onSuccess(Hooks<Server> hooks) {
        checkStart();

        this.onSuccessHooks = hooks;
        return this;
    }

    @Override
    public Server onFailure(final Hooks<Throwable> hooks) {
        checkStart();

        this.onFailureHooks = hooks;
        return this;
    }

    @Override
    public Server withSerializer(final JsonSerializer serializer) {
        checkStart();

        this.serializer = serializer;
        return this;
    }

    @Override
    public Server withInitChannel(Hooks<SocketChannel> hooks) {
        checkStart();
        socketChannelInitHooks = hooks;
        return this;
    }

    @Override
    public ChannelInitializer<SocketChannel> channelInitializer() {
        checkNotStart();
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
    public ChannelFuture dispose() {
        return startFuture.channel().closeFuture().syncUninterruptibly();
    }

    @Override
    public void close() {
        checkNotStart();
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
    public JsonSerializer serializer() {
        return this.serializer;
    }

    @Override
    public Server impl() {
        return this;
    }

    @Override
    public Server withWorker(final EventLoopGroup worker) {
        checkStart();
        this.worker = worker;
        return this;
    }

    @Override
    public Server withLog(final LoggingHandler loggingHandler) {
        checkStart();
        this.loggingHandler = loggingHandler;
        return this;
    }

    public Server withGroup(final int bossCount, final int workerCount) {
        checkStart();
        this.boss = new NioEventLoopGroup(bossCount);
        this.worker = new NioEventLoopGroup(workerCount);
        return this;
    }

    public Server withGroup(final EventLoopGroup boss, final EventLoopGroup worker) {
        checkStart();
        this.boss = boss;
        this.worker = worker;
        return this;
    }

    public Server withChannelType(Class<? extends ServerChannel> channelType) {
        checkStart();
        this.channelType = channelType;
        return this;
    }

    public <T> Server withServerOptions(ChannelOption<T> option, T t) {
        checkStart();
        serverOptions.put(option, t);
        return this;
    }

    public <T> Server withChildOptions(ChannelOption<T> option, T t) {
        checkStart();
        childOptions.put(option, t);
        return this;
    }

    public Server withServerHandler(ChannelHandler handler) {
        checkStart();
        serverHandlers.add(handler);
        return this;
    }

    public Server withSsl(final SslHandler sslHandler) {
        checkStart();
        this.sslHandler = sslHandler;
        this.sslFlag = true;
        return this;
    }

    public Server withBoss(final int bossCount) {
        checkStart();
        return withBoss(new NioEventLoopGroup(bossCount));
    }

    public Server withBoss(final EventLoopGroup boss) {
        checkStart();
        this.worker = boss;
        return this;
    }

    public int port() {
        return this.address.getPort();
    }

    private void checkStart() {
        if (startFlag) {
            throw new IllegalStateException("client is already started");
        }
    }

    private void preStart(final InetSocketAddress address) {
        this.startFlag = true;
        Objects.requireNonNull(address, "address is null");
        Objects.requireNonNull(channelType, "channelType is null");
        this.address = address;

        if (this.boss == null) {
            this.boss = new NioEventLoopGroup();
        }

        if (this.worker == null) {
            this.worker = new NioEventLoopGroup();
        }

        if (this.serializer == null) {
            this.serializer = JsonSerializer.codec;
        }
    }

    private void checkNotStart() {
        if (!startFlag) {
            throw new IllegalStateException("client is not started");
        }
    }
}