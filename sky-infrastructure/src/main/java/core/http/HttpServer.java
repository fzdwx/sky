package core.http;

import core.Server;
import core.Transport;
import core.common.Disposer;
import core.http.ext.HttpExceptionHandler;
import core.http.ext.HttpHandler;
import core.http.handler.AccessLogHandler;
import core.http.handler.BodyHandler;
import core.http.handler.HttpServerHandler;
import core.serializer.JsonSerializer;
import io.github.fzdwx.lambada.Console;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import util.Netty;
import util.Utils;

import java.net.InetSocketAddress;

/**
 * http server implementation
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/6 16:19
 */
@Slf4j
public class HttpServer implements Transport<HttpServer> {

    private final Server server;
    private boolean sslFlag;
    private int maxContentLength;
    private HttpHandler httpHandler;
    private HttpExceptionHandler exceptionHandler;
    private Hooks<ChannelFuture> afterListenHooks;
    private boolean enableAccessLog = false;


    private HttpServer() {
        this.server = new Server();

        this.serverOptions(ChannelOption.SO_BACKLOG, 1024);
        this.childOptions(ChannelOption.TCP_NODELAY, true);
        this.childOptions(ChannelOption.SO_KEEPALIVE, true);
        this.childOptions(ChannelOption.SO_REUSEADDR, true);
    }

    private HttpServer(Server server) {
        this.server = server;
    }

    /**
     * use {@link Server}.
     */
    public static HttpServer create() {
        return new HttpServer();
    }

    /**
     * customization server impl,change HTTP Server core.
     */
    public static HttpServer create(Server server) {
        return new HttpServer(server);
    }

    /**
     * listen on a random port
     */
    public Disposer listen() {
        this.server.listen();
        return this;
    }

    /**
     * start server
     */
    public Disposer listen(final int port) {
        return this.listen(new InetSocketAddress(Netty.localhost, port));
    }

    @Override
    public Disposer listen(final InetSocketAddress address) {
        if (this.maxContentLength == 0) {
            this.maxContentLength = Netty.DEFAULT_MAX_CONTENT_LENGTH;
        }
        final String serverOrigin = getServerOrigin(address);

        this.server.afterListen(f -> {
            if (f.isSuccess()) {
                log.info(Console.cyan(Utils.PREFIX) + "HTTP server started Listen on " + serverOrigin);
            }

            if (this.afterListenHooks != null) {
                this.afterListenHooks.call(f);
            }
        });

        this.server.childHandler(channel -> {
                    final ChannelPipeline p = channel.pipeline();
                    p.addLast(new HttpServerCodec())
                            .addLast(new HttpContentDecompressor(false))
                            // todo 请求压缩
                            // .addLast(new HttpContentCompressor())
                            .addLast(new ChunkedWriteHandler())
                            .addLast(new HttpServerExpectContinueHandler())
                            .addLast(BodyHandler.create(this.maxContentLength));
                    if (enableAccessLog) {
                        p.addLast(new AccessLogHandler());
                    }
                    p.addLast(new HttpObjectAggregator(maxContentLength))
                            .addLast(new HttpServerHandler(httpHandler, exceptionHandler, sslFlag, jsonSerializer()));


                })
                .listen(address);

        return this;
    }

    /**
     * after start.
     */
    public HttpServer afterListen(Hooks<ChannelFuture> hooks) {
        this.afterListenHooks = hooks;
        return this;
    }

    @Override
    public HttpServer onSuccess(final Hooks<HttpServer> hooks) {
        this.server.onSuccess(server -> {
            hooks.call(this);
        });
        return this;
    }

    @Override
    public HttpServer onFailure(final Hooks<Throwable> hooks) {
        this.server.onFailure(hooks);
        return this;
    }

    @Override
    public HttpServer onShutDown(final Hooks<ChannelFutureListener> hooks) {
        this.server.onShutDown(hooks);
        return this;
    }

    /**
     * default is {@link JsonSerializer#codec}
     */
    public HttpServer jsonSerializer(final JsonSerializer serializer) {
        this.server.jsonSerializer(serializer);
        return this;
    }

    @Override
    public HttpServer childHandler(final Hooks<SocketChannel> hooks) {
        this.server.childHandler(hooks);
        return this;
    }

    @Override
    public void shutdown() {
        this.server.shutdown();
    }

    @Override
    public ChannelFuture close() {
        return server.close();
    }

    @Override
    public boolean ssl() {
        return this.server.ssl();
    }

    public JsonSerializer jsonSerializer() {
        return this.server.jsonSerializer();
    }

    @Override
    public HttpServer impl() {
        return this;
    }

    @Override
    public HttpServer worker(final int worker) {
        this.server.worker(worker);
        return this;
    }

    /**
     * enable accessLog
     */
    public HttpServer accessLog() {
        this.enableAccessLog = true;
        return this;
    }

    /**
     * @see #log(LoggingHandler)
     */
    public HttpServer log(final LogLevel logLevel) {
        return this.log(new LoggingHandler(logLevel));
    }

    /**
     * ser child log handler
     */
    public HttpServer log(final LoggingHandler loggingHandler) {
        this.server.log(loggingHandler);
        return this;
    }

    public ChannelFuture dispose() {
        return this.server.dispose();
    }

    /**
     * Set the maximum file length that can be processed, if it exceeds this value, it will throw 413 Request Entity Too Large
     *
     * @see io.netty.handler.codec.http.HttpObjectAggregator
     */
    public HttpServer maxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return this;
    }

    /**
     * add server option
     */
    public <T> HttpServer serverOptions(ChannelOption<T> option, T t) {
        this.server.serverOptions(option, t);
        return this;
    }

    /**
     * add child option
     */
    public <T> HttpServer childOptions(ChannelOption<T> option, T t) {
        this.server.childOptions(option, t);
        return this;
    }

    /**
     * @see io.netty.bootstrap.ServerBootstrap#attr(AttributeKey, Object)
     */
    public <T> HttpServer attr(AttributeKey<T> key, T val) {
        this.server.attr(key, val);
        return this;
    }

    /**
     * @see io.netty.bootstrap.ServerBootstrap#childAttr(AttributeKey, Object)
     */
    public <T> HttpServer childAttr(AttributeKey<T> key, T val) {
        this.server.childAttr(key, val);
        return this;
    }

    /**
     * add server handler
     */
    public HttpServer serverHandler(ChannelHandler handler) {
        this.server.serverHandler(handler);
        return this;
    }

    /**
     * enable ssl.
     */
    public HttpServer ssl(final SslHandler sslHandler) {
        this.server.ssl(sslHandler);
        this.sslFlag = true;
        return this;
    }

    /**
     * handler request and return response.
     */
    public HttpServer requestHandler(final HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
        return this;
    }

    /**
     * handler exception.
     */
    public HttpServer exceptionHandle(HttpExceptionHandler h) {
        this.exceptionHandler = h;
        return this;
    }

    public String scheme() {
        return this.sslFlag ? "https" : "http";
    }

    public int port() {
        return this.server.port();
    }

    private String getServerOrigin(final InetSocketAddress address) {
        return scheme() + "://" + address.getHostString() + ":" + address.getPort();
    }
}