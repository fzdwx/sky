package core;

import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;
import serializer.JsonSerializer;
import util.AvailablePort;
import util.Utils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * server.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 14:35
 */
@Slf4j
public class Server implements Transport<Server> {

    private final Map<ChannelOption<?>, Object> serverOptions = new HashMap<>();
    private final Map<ChannelOption<?>, Object> childOptions = new HashMap<>();
    private final List<ChannelHandler> serverHandlers = new java.util.ArrayList<>();
    private Hooks<SocketChannel> socketChannelInitHooks;
    private Hooks<ChannelFuture> afterListen;
    private Hooks<Server> onSuccessHooks;
    private Hooks<Throwable> onFailureHooks;
    private EventLoopGroup worker;
    private ServerBootstrap bootstrap;
    private boolean sslFlag;
    private ChannelFuture startFuture;
    private LoggingHandler loggingHandler;
    private SslHandler sslHandler;
    private InetSocketAddress address;
    private JsonSerializer serializer;
    private AtomicBoolean startFlag = new AtomicBoolean(false);
    private final boolean enableEpoll;
    private Class<? extends ServerChannel> channelType = NioServerSocketChannel.class;
    private EventLoopGroup boss;

    public Server() {
        this.bootstrap = new ServerBootstrap();
        this.enableEpoll = Epoll.isAvailable();
        if (enableEpoll) {
            log.info(Utils.PREFIX + " use epoll");
        }
    }

    /**
     * listen on a random port
     */
    public Server listen() {
        final Integer port = AvailablePort.random();
        Assert.nonNull(port, "now don't have available port");

        return listen(port);
    }

    @Override
    public Server listen(final int port) {
        if (port == 0) {
            return listen();
        }
        return Transport.super.listen(port);
    }

    @Override
    public Server listen(final InetSocketAddress address) {
        preStart(address);

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
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                if (loggingHandler != null) {
                    ch.pipeline().addLast(loggingHandler);
                }
                if (sslHandler != null) {
                    ch.pipeline().addLast(sslHandler);
                }

                if (socketChannelInitHooks != null) {
                    socketChannelInitHooks.call(ch);
                }
            }
        };
    }

    @Override
    public ChannelFuture dispose() {
        return startFuture.channel().closeFuture().syncUninterruptibly();
    }

    @Override
    public void shutdown() {
        checkNotStart();
        if (!this.worker.isShutdown()) {
            this.worker.shutdownGracefully();
        }
        if (!this.boss.isShutdown()) {
            this.boss.shutdownGracefully();
        }
    }

    @Override
    public ChannelFuture close() {
        return startFuture.channel().close();
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
    public Server withWorker(final int worker) {
        checkStart();
        this.worker = createWorker(worker);
        return this;
    }

    @Override
    public Server withLog(final LoggingHandler loggingHandler) {
        checkStart();
        this.loggingHandler = loggingHandler;
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

    public int port() {
        return this.address.getPort();
    }

    private void checkStart() {
        if (startFlag.get()) {
            throw new IllegalStateException("server is already started,don't support this action!");
        }
    }

    private void preStart(final InetSocketAddress address) {
        if (!startFlag.compareAndSet(false, true)) {
            return;
        }

        Objects.requireNonNull(address, "address is null");
        Objects.requireNonNull(channelType, "channelType is null");
        this.address = address;

        this.boss = createBoss(1);

        if (this.worker == null) {
            this.worker = createWorker(0);
        }

        if (this.serializer == null) {
            this.serializer = JsonSerializer.codec;
        }

        if (serverHandlers.size() > 0) {
            serverHandlers.forEach(bootstrap::handler);
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : serverOptions.entrySet()) {
            this.bootstrap = bootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.bootstrap = bootstrap.childOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }
    }

    private void checkNotStart() {
        if (!startFlag.get()) {
            throw new IllegalStateException("server is not started,don't support this action!");
        }
    }

    private EventLoopGroup createBoss(final int bossCount) {
        EventLoopGroup group;
        if (this.enableEpoll) {
            group = new EpollEventLoopGroup(bossCount, new SkyThreadFactory("Epoll-Sky-Server-Boss"));
        } else {
            group = new NioEventLoopGroup(bossCount, new SkyThreadFactory("NIO-Sky-Server-Boss"));
        }
        return group;
    }

    private EventLoopGroup createWorker(final int workerCnt) {
        EventLoopGroup group;
        if (this.enableEpoll) {
            group = new EpollEventLoopGroup(workerCnt, new SkyThreadFactory("Epoll-Sky-Server-Boss"));
        } else {
            group = new NioEventLoopGroup(workerCnt, new SkyThreadFactory("NIO-Sky-Server-Boss"));
        }
        return group;
    }
}