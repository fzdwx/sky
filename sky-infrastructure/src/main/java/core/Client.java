package core;

import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import serializer.JsonSerializer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/11 15:10
 */
public class Client implements Transport<Client> {

    private Bootstrap bootstrap;
    private EventLoopGroup worker;
    private JsonSerializer serializer;
    private LoggingHandler loggingHandler;
    private boolean sslFlag;
    private InetSocketAddress address;
    private Hooks<SocketChannel> socketChannelInitHooks;
    private ChannelFuture startFuture;
    private Class<? extends Channel> channelType = NioSocketChannel.class;

    private final Map<ChannelOption<?>, Object> childOptions = new HashMap<>();

    public Client() {
        this.bootstrap = new Bootstrap();
    }

    @Override
    public Client start(final InetSocketAddress address) {
        preStart(address);

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.bootstrap = bootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        this.startFuture = bootstrap
                .group(worker)
                .channel(channelType)
                .handler(channelInitializer())
                .connect(address);

        return this;
    }

    public <T> Client withOptions(ChannelOption<T> option, T t) {
        childOptions.put(option, t);
        return this;
    }

    @Override
    public Client withWorkerGroup(final EventLoopGroup worker) {
        this.worker = worker;
        return this;
    }

    @Override
    public Client withSerializer(final JsonSerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    /**
     * use ssl connect
     */
    public Client withSsl() {
        this.sslFlag = true;
        return this;
    }

    @Override
    public ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<>() {
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
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                    @Override
                    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                        ctx.writeAndFlush("hell world");
                    }

                    @Override
                    public void channelReadComplete(ChannelHandlerContext ctx) {
                        ctx.flush();
                    }

                    @Override
                    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                        System.out.println("client receive: " + msg);
                    }
                });
                // socketChannelInitHooks.call(ch);
            }
        };
    }

    @Override
    public Client withLog(final LoggingHandler loggingHandler) {
        this.loggingHandler = loggingHandler;
        return this;
    }

    @Override
    public Client withInitChannel(final Hooks<SocketChannel> hooks) {
        this.socketChannelInitHooks = hooks;
        return this;
    }

    /**
     * @apiNote default is {@link NioSocketChannel}
     */
    public Client withChannelType(final Class<? extends Channel> channelType) {
        this.channelType = channelType;
        return this;
    }

    @Override
    public ChannelFuture dispose() {
        return startFuture.channel().closeFuture().syncUninterruptibly();
    }

    @Override
    public void close() {
        this.worker.shutdownGracefully();
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
    public Client impl() {
        return this;
    }

    private void preStart(final InetSocketAddress address) {
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
}