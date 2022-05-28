package core;

import core.serializer.JsonSerializer;
import core.thread.SkyThreadFactory;
import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.Collections;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import util.AvailablePort;
import util.Utils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
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
public class Server implements core.Transport<Server> {

    protected final AtomicBoolean startFlag = new AtomicBoolean(false);
    protected final Map<ChannelOption<?>, Object> serverOptions = new HashMap<>();
    protected final Map<ChannelOption<?>, Object> childOptions = new HashMap<>();
    protected final List<ChannelHandler> serverHandlers = new ArrayList<>();
    protected final List<Hooks<SocketChannel>> scInit = new ArrayList<>();
    protected Map<AttributeKey<?>, Object> attrMap = Collections.map();
    protected Map<AttributeKey<?>, Object> childAttrMap = Collections.map();
    protected final boolean enableEpoll;
    protected Class<? extends ServerChannel> channelType;
    protected Hooks<ChannelFuture> afterListen;
    protected Hooks<Server> onSuccessHooks;
    protected Hooks<Throwable> onFailureHooks;
    protected Hooks<ChannelFutureListener> onShutDownHooks;
    protected JsonSerializer serializer;
    protected EventLoopGroup boss;
    protected EventLoopGroup worker;
    protected InetSocketAddress address;
    protected LoggingHandler loggingHandler;
    protected SslHandler sslHandler;
    protected boolean sslFlag;
    protected int port;
    protected ChannelFuture startFuture;
    protected Channel channel;
    protected ServerBootstrap serverBootstrap;

    public Server() {
        this.serverBootstrap = new ServerBootstrap();
        this.enableEpoll = Epoll.isAvailable();

        if (enableEpoll) {
            this.channelType = EpollServerSocketChannel.class;
            log.info(Utils.PREFIX + " use epoll");
        } else {
            this.channelType = NioServerSocketChannel.class;
            log.info(Utils.PREFIX + " use nio");
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
        preListen(address);

        this.startFuture = this.serverBootstrap
                .childHandler(buildWorkerHandler())
                .channel(this.channelType)
                .group(this.boss, this.worker)
                .bind(address)
                .syncUninterruptibly();

        this.startFuture
                .addListener(f -> {
                    if (this.afterListen != null) {
                        this.afterListen.call(startFuture);
                    }

                    if (f.isSuccess()) {
                        if (this.onSuccessHooks != null) {
                            this.onSuccessHooks.call(impl());
                        }
                    } else {
                        if (this.onFailureHooks != null) {
                            this.onFailureHooks.call(f.cause());
                        }
                    }
                });


        this.channel = this.startFuture.channel();

        return impl();
    }

    @Override
    public ChannelFuture dispose() {
        return startFuture.channel().closeFuture().syncUninterruptibly();
    }

    @Override
    public void shutdown() {
        checkNotStart();

        close().addListener(f -> {
            if (!this.worker.isShutdown()) {
                this.worker.shutdownGracefully();
            }
            if (!this.boss.isShutdown()) {
                this.boss.shutdownGracefully();
            }

            onShutDownHooks.call((ChannelFutureListener) f);
        });
    }

    @Override
    public ChannelFuture close() {
        return startFuture.channel().close();
    }

    public Server boss(final int bossCount) {
        checkStart();
        this.boss = createBoss(bossCount);
        return impl();
    }

    @Override
    public Server worker(final int worker) {
        checkStart();
        this.worker = createWorker(worker);
        return impl();
    }

    @Override
    public Server log(final LoggingHandler loggingHandler) {
        checkStart();
        this.loggingHandler = loggingHandler;
        return impl();
    }

    public Server ssl(final SslHandler sslHandler) {
        checkStart();
        this.sslHandler = sslHandler;
        this.sslFlag = true;
        return impl();
    }

    public Server channelType(Class<? extends ServerChannel> channelType) {
        checkStart();
        this.channelType = channelType;
        return impl();
    }

    @Override
    public Server jsonSerializer(final JsonSerializer serializer) {
        checkStart();

        this.serializer = serializer;
        return impl();
    }

    public <T> Server serverOptions(ChannelOption<T> option, T t) {
        checkStart();
        serverOptions.put(option, t);
        return this;
    }

    public <T> Server childOptions(ChannelOption<T> option, T t) {
        checkStart();
        childOptions.put(option, t);

        return this;
    }

    public <T> Server attr(AttributeKey<T> key, T val) {
        this.attrMap.put(key, val);
        return this;
    }

    public <T> Server childAttr(AttributeKey<T> key, T val) {
        this.childAttrMap.put(key, val);
        return this;
    }

    public Server serverHandler(ChannelHandler handler) {
        checkStart();
        serverHandlers.add(handler);
        return this;
    }

    @Override
    public Server addSocketChannelHooks(Hooks<SocketChannel> hooks) {
        Assert.nonNull(hooks, "socket channel init is null!");
        checkStart();

        scInit.add(hooks);

        return impl();
    }

    @Override
    public JsonSerializer jsonSerializer() {
        return this.serializer;
    }

    @Override
    public Server impl() {
        return this;
    }

    @Override
    public Server afterListen(Hooks<ChannelFuture> hooks) {
        checkStart();

        this.afterListen = hooks;
        return impl();
    }

    @Override
    public Server onSuccess(Hooks<Server> hooks) {
        checkStart();

        this.onSuccessHooks = hooks;
        return impl();
    }

    @Override
    public Server onFailure(final Hooks<Throwable> hooks) {
        checkStart();

        this.onFailureHooks = hooks;
        return impl();
    }

    public Server onShutDown(final Hooks<ChannelFutureListener> hooks) {
        checkStart();

        this.onShutDownHooks = hooks;
        return impl();
    }

    @Override
    public boolean ssl() {
        return this.sslFlag;
    }

    public int port() {
        return port;
    }

    protected void preListen(final InetSocketAddress address) {
        if (!startFlag.compareAndSet(false, true)) {
            return;
        }
        Objects.requireNonNull(address, "address is null");
        Objects.requireNonNull(channelType, "channelType is null");

        this.address = address;

        if (this.boss == null) {
            this.boss = createBoss(0);
        }
        if (this.worker == null) {
            this.worker = createWorker(0);
        }

        if (this.serializer == null) {
            this.serializer = JsonSerializer.codec;
        }
        if (serverHandlers.size() > 0) {
            serverHandlers.forEach(serverBootstrap::handler);
        }

        for (final Map.Entry<AttributeKey<?>, ?> attr : this.attrMap.entrySet()) {
            this.serverBootstrap = serverBootstrap.attr((AttributeKey<Object>) attr.getKey(), attr.getValue());
        }

        for (final Map.Entry<AttributeKey<?>, ?> attr : this.childAttrMap.entrySet()) {
            this.serverBootstrap = serverBootstrap.childAttr((AttributeKey<Object>) attr.getKey(), attr.getValue());
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : serverOptions.entrySet()) {
            this.serverBootstrap = serverBootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.serverBootstrap = serverBootstrap.childOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        this.port = this.address.getPort();
    }

    protected ChannelInitializer<SocketChannel> buildWorkerHandler() {
        checkNotStart();
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(@NotNull final SocketChannel ch) {
                if (Server.this.loggingHandler != null) {
                    ch.pipeline().addLast(Server.this.loggingHandler);
                }
                if (Server.this.sslHandler != null) {
                    ch.pipeline().addLast(Server.this.sslHandler);
                }

                for (final Hooks<SocketChannel> hooks : Server.this.scInit) {
                    hooks.call(ch);
                }
            }
        };
    }

    protected EventLoopGroup createBoss(final int bossCount) {
        EventLoopGroup group;
        if (this.enableEpoll) {
            group = new EpollEventLoopGroup(bossCount, new SkyThreadFactory("Epoll-Sky-Server-Boss"));
        } else {
            group = new NioEventLoopGroup(bossCount, new SkyThreadFactory("NIO-Sky-Server-Boss"));
        }
        return group;
    }

    protected EventLoopGroup createWorker(final int workerCnt) {
        EventLoopGroup group;
        if (this.enableEpoll) {
            group = new EpollEventLoopGroup(workerCnt, new SkyThreadFactory("Epoll-Sky-Server-Boss"));
        } else {
            group = new NioEventLoopGroup(workerCnt, new SkyThreadFactory("NIO-Sky-Server-Boss"));
        }
        return group;
    }

    protected final void checkStart() {
        if (startFlag.get()) {
            throw new IllegalStateException("server is already started,don't support this action!");
        }
    }

    protected void checkNotStart() {
        if (!startFlag.get()) {
            throw new IllegalStateException("server is not started,don't support this action!");
        }
    }

}