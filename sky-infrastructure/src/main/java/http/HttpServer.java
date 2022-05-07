package http;

import core.Server;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 16:19
 */
public class HttpServer {

    private final Server server;
    private HttpRequestConsumer consumer;

    private boolean sslFlag = false;
    private HttpDataFactory httpDataFactory = null;

    public HttpServer() {
        this.server = new Server();
    }

    public <T> HttpServer withServerOptions(ChannelOption<T> option, T t) {
        server.withServerOptions(option, t);
        return this;
    }

    public <T> HttpServer withChildOptions(ChannelOption<T> option, T t) {
        server.withChildOptions(option, t);
        return this;
    }

    public HttpServer withGroup(final int bossCount, final int workerCount) {
        this.server.withGroup(bossCount, workerCount);
        return this;
    }

    public HttpServer withGroup(final EventLoopGroup boss, final EventLoopGroup worker) {
        this.server.withGroup(boss, worker);
        return this;
    }

    public HttpServer withLog(final LoggingHandler loggingHandler) {
        server.withLog(loggingHandler);
        return this;
    }

    public HttpServer withSsl(final SslHandler sslHandler) {
        server.withSsl(sslHandler);
        this.sslFlag = true;
        return this;
    }

    public HttpServer withHttpDataFactory(final HttpDataFactory factory) {
        this.httpDataFactory = factory;
        return this;
    }

    public HttpServer handler(final HttpRequestConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public HttpServer bind(final int port) {
        this.withServerOptions(ChannelOption.SO_BACKLOG, 1024);
        this.withChildOptions(ChannelOption.TCP_NODELAY, true);
        this.withChildOptions(ChannelOption.SO_KEEPALIVE, true);

        server.bind(port)
                .withInitChannel(channel -> {
                    channel.pipeline().addLast(new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(1024 * 1024))
                            .addLast(new ChunkedWriteHandler())
                            .addLast(new HttpServerExpectContinueHandler())
                            .addLast(new HttpServerHandler(consumer, sslFlag, httpDataFactory));
                });

        return this;
    }

    public HttpServer afterStart(Hooks<ChannelFuture> hooks) {
        server.afterStart(hooks);
        return this;
    }

    public String scheme() {
        return sslFlag ? "https" : "http";
    }
}