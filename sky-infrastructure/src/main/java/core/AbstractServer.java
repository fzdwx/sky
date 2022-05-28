package core;

import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import serializer.JsonSerializer;
import thread.SkyThreadFactory;
import util.AvailablePort;
import util.Utils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/28 10:36
 */
@Slf4j
public abstract class AbstractServer<IMPL> implements Transport<IMPL> {

    protected final AtomicBoolean startFlag = new AtomicBoolean(false);
    protected final List<Hooks<SocketChannel>> scInit = new ArrayList<>();
    protected final boolean enableEpoll;
    protected Class<? extends ServerChannel> channelType;
    protected Hooks<ChannelFuture> afterListen;
    protected Hooks<IMPL> onSuccessHooks;
    protected Hooks<Throwable> onFailureHooks;
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

    protected AbstractServer() {
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
    public IMPL listen() {
        final Integer port = AvailablePort.random();
        Assert.nonNull(port, "now don't have available port");

        return listen(port);
    }

    @Override
    public IMPL listen(final int port) {
        if (port == 0) {
            return listen();
        }
        return Transport.super.listen(port);
    }

    @Override
    public IMPL listen(final InetSocketAddress address) {
        preListen(address);

        this.serverBootstrap
                .childHandler(channelInitializer())
                .channel(channelType)
                .group(boss, worker)
                .bind(address)
                .syncUninterruptibly()
                .addListener(f -> {
                    if (afterListen != null) {
                        afterListen.call(startFuture);
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


        channel = startFuture.channel();

        return impl();
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

    public IMPL withBoss(final int bossCount) {
        checkStart();
        this.boss = createBoss(bossCount);
        return impl();
    }

    @Override
    public IMPL withWorker(final int worker) {
        checkStart();
        this.worker = createWorker(worker);
        return impl();
    }

    @Override
    public IMPL withLog(final LoggingHandler loggingHandler) {
        checkStart();
        this.loggingHandler = loggingHandler;
        return impl();
    }

    public IMPL withSsl(final SslHandler sslHandler) {
        checkStart();
        this.sslHandler = sslHandler;
        this.sslFlag = true;
        return impl();
    }

    public IMPL withChannelType(Class<? extends ServerChannel> channelType) {
        checkStart();
        this.channelType = channelType;
        return impl();
    }

    @Override
    public IMPL withSerializer(final JsonSerializer serializer) {
        checkStart();

        this.serializer = serializer;
        return impl();
    }

    @Override
    public IMPL withInitChannel(Hooks<SocketChannel> hooks) {
        Assert.nonNull(hooks, "socket channel init is null!");
        checkStart();

        scInit.add(hooks);

        return impl();
    }

    @Override
    public ChannelInitializer<SocketChannel> channelInitializer() {
        checkNotStart();
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(@NotNull final SocketChannel ch) throws Exception {
                if (loggingHandler != null) {
                    ch.pipeline().addLast(loggingHandler);
                }
                if (sslHandler != null) {
                    ch.pipeline().addLast(sslHandler);
                }

                for (final Hooks<SocketChannel> hooks : scInit) {
                    hooks.call(ch);
                }
            }
        };
    }

    @Override
    public JsonSerializer jsonSerializer() {
        return this.serializer;
    }

    @Override
    public IMPL afterListen(Hooks<ChannelFuture> hooks) {
        checkStart();

        this.afterListen = hooks;
        return impl();
    }

    @Override
    public IMPL onSuccess(Hooks<IMPL> hooks) {
        checkStart();

        this.onSuccessHooks = hooks;
        return impl();
    }

    @Override
    public IMPL onFailure(final Hooks<Throwable> hooks) {
        checkStart();

        this.onFailureHooks = hooks;
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

        this.port = this.address.getPort();
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