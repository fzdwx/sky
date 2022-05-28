package core;

import core.thread.SkyThreadFactory;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import core.serializer.JsonSerializer;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/11 15:10
 */
@Slf4j
public class Client implements Transport<Client> {

    private final int DEFAULT_MAX_RECONNECT_TIMES = 3;
    private final Duration DEFAULT_RECONNECT_TIMEOUT = Duration.ofSeconds(1);
    private final Map<ChannelOption<?>, Object> childOptions = new HashMap<>();
    public boolean startFlag = false;
    private int maxReconnectTimes;
    private int reconnectTimes = 0;
    private Duration reconnectTimeout;
    private boolean enableAutoReconnect = false;
    private Bootstrap bootstrap;
    private EventLoopGroup worker;
    private JsonSerializer serializer;
    private LoggingHandler loggingHandler;
    private boolean sslFlag;
    private InetSocketAddress address;
    private Hooks<SocketChannel> socketChannelInitHooks;
    private ChannelFuture startFuture;
    private Channel channel;
    private Class<? extends Channel> channelType = NioSocketChannel.class;
    private Hooks<ChannelFuture> afterListen;
    private Hooks<Client> onSuccessHooks;
    private Hooks<Throwable> onFailureHooks;
    private boolean enableEpoll;

    public Client() {
        this.bootstrap = new Bootstrap();
        this.enableEpoll = Epoll.isAvailable();
    }

    @Override
    public Client listen(final InetSocketAddress address) {
        preStart(address);

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.bootstrap = bootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        bootstrap.group(worker).channel(channelType).handler(workerHandler());

        connect();

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
    public Client afterListen(final Hooks<ChannelFuture> hooks) {
        checkStart();

        this.afterListen = hooks;
        return this;
    }

    @Override
    public Client onSuccess(final Hooks<Client> hooks) {
        checkStart();

        this.onSuccessHooks = hooks;
        return this;
    }

    @Override
    public Client onFailure(final Hooks<Throwable> hooks) {
        checkStart();

        this.onFailureHooks = hooks;
        return this;
    }

    @Override
    public Client jsonSerializer(final JsonSerializer serializer) {
        checkStart();
        this.serializer = serializer;
        return this;
    }

    @Override
    public Client addSocketChannelHooks(final Hooks<SocketChannel> hooks) {
        checkStart();
        this.socketChannelInitHooks = hooks;
        return this;
    }

    @Override
    public ChannelInitializer<SocketChannel> workerHandler() {
        checkNotStart();
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                if (loggingHandler != null) {
                    ch.pipeline().addLast(loggingHandler);
                }

                if (sslFlag) {
                    final SslHandler sslHandler = SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
                            .newHandler(ch.alloc(), address.getHostName(), address.getPort());

                    ch.pipeline().addLast(sslHandler);
                }

                if (enableAutoReconnect) {
                    ch.pipeline().addFirst(new ReconnectHandler(Client.this::reconnect));
                }
                socketChannelInitHooks.call(ch);
            }
        };
    }

    @Override
    public ChannelFuture dispose() {
        checkNotStart();
        return startFuture.channel().closeFuture().syncUninterruptibly();
    }

    @Override
    public void shutdown() {
        checkNotStart();
        this.worker.shutdownGracefully();
    }

    @Override
    public ChannelFuture close() {
        return startFuture.channel().close();
    }

    @Override
    public boolean ssl() {
        return this.sslFlag;
    }

    @Override
    public JsonSerializer jsonSerializer() {
        return this.serializer;
    }

    @Override
    public Client impl() {
        return this;
    }

    @Override
    public Client worker(final int worker) {
        checkStart();
        this.worker = createWorker(worker);
        return this;
    }

    @Override
    public Client log(final LoggingHandler loggingHandler) {
        checkStart();
        this.loggingHandler = loggingHandler;
        return this;
    }

    public <T> Client withOptions(ChannelOption<T> option, T t) {
        checkStart();
        childOptions.put(option, t);
        return this;
    }

    /**
     * use ssl connect
     */
    public Client withSsl() {
        checkStart();
        this.sslFlag = true;
        return this;
    }

    /**
     * @apiNote default is {@link NioSocketChannel}
     */
    public Client withChannelType(final Class<? extends Channel> channelType) {
        checkStart();
        this.channelType = channelType;
        return this;
    }

    /**
     * @see #withEnableAutoReconnect(int, Duration)
     */
    public Client withEnableAutoReconnect() {
        return withEnableAutoReconnect(DEFAULT_MAX_RECONNECT_TIMES, DEFAULT_RECONNECT_TIMEOUT);
    }

    /**
     * 开启断线重连
     *
     * @param maxReconnectTimes 最大重连次数
     * @param reconnectTimeout  连接超时时间
     * @return {@link Client }
     */
    public Client withEnableAutoReconnect(int maxReconnectTimes, Duration reconnectTimeout) {
        checkStart();
        this.enableAutoReconnect = true;
        this.maxReconnectTimes = maxReconnectTimes;
        this.reconnectTimeout = reconnectTimeout;
        return this;
    }

    /**
     * @see #withEnableAutoReconnect(int, Duration)
     */
    public Client withEnableAutoReconnect(int maxReconnectTimes) {
        return withEnableAutoReconnect(maxReconnectTimes, DEFAULT_RECONNECT_TIMEOUT);
    }

    /**
     * @see #withEnableAutoReconnect(int, Duration)
     */
    public Client withEnableAutoReconnect(Duration reconnectTimeout) {
        return withEnableAutoReconnect(DEFAULT_MAX_RECONNECT_TIMES, reconnectTimeout);
    }

    void connect() {
        connect(false);
    }

    void reconnect() {
        connect(true);
    }

    void connect(final boolean reconnectFlag) {
        if (channel != null && channel.isActive()) {
            return;
        }

        startFuture = bootstrap.connect(address);

        // reconnect
        startFuture.addListener((ChannelFutureListener) futureListener -> {
            if (reconnectFlag) {
                reconnectTimes++;
            }

            if (futureListener.isSuccess()) {
                channel = futureListener.channel();

                log.info("connect to {} success", address);
                this.reconnectTimes = 0;
            } else {
                log.error("connect to {} failed", address, futureListener.cause());

                if (reconnectTimes > maxReconnectTimes) {
                    log.error("connect to {} failed, max reconnect times {}", address, maxReconnectTimes);
                    return;
                }

                if (reconnectFlag) {
                    log.info("reconnect times:{},maxTimes:{},delay:{}", reconnectTimes, maxReconnectTimes, this.reconnectTimeout.toString());
                } else {
                    log.info("reconnect delay:{}", this.reconnectTimeout.toString());
                }

                futureListener.channel().eventLoop().schedule(() -> {
                    Client.this.connect(reconnectFlag);
                }, this.reconnectTimeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        });
    }

    private void checkNotStart() {
        if (!startFlag) {
            throw new IllegalStateException("client is not started");
        }
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

        if (this.worker == null) {
            this.worker = new NioEventLoopGroup();
        }

        if (this.serializer == null) {
            this.serializer = JsonSerializer.codec;
        }
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