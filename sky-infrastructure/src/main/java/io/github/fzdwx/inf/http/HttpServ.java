package io.github.fzdwx.inf.http;

import io.github.fzdwx.inf.core.ServInf;
import io.github.fzdwx.inf.http.core.HttpServerHandler;
import io.github.fzdwx.inf.http.inter.HttpDevHtml;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static java.net.InetAddress.getLocalHost;

/**
 * http serv.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 11:25
 * @since 0.06
 */
@Slf4j
public class HttpServ extends ServInf<HttpServ> {

    private final Router router;
    private boolean dev;

    private HttpDataFactory httpDataFactory;

    /**
     * http server
     *
     * @param port   the listening port
     * @param router router
     */
    public HttpServ(final int port, final Router router) {
        super(port);
        this.router = router;
    }

    public String scheme() {
        return ssl() ? "https" : "http";
    }

    /**
     * mount http data factory.
     *
     * @apiNote default is <pre> {@code
     *  new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE)
     * }
     * </pre>
     * @see DefaultHttpDataFactory#MINSIZE
     * @see io.netty.handler.codec.http.multipart.DefaultHttpDataFactory
     */
    public HttpServ mountHttpDataFactory(HttpDataFactory factory) {
        this.httpDataFactory = factory;

        return this;
    }

    /**
     * open dev mode
     *
     * @param staticPath static file path; e.g. static/
     */
    public HttpServ dev(final String staticPath) {
        this.dev = true;

        // Add dev html page
        router.GET(HttpDevHtml.PAGE_PATH, new HttpDevHtml(this.name, router, staticPath));

        return this;
    }

    /**
     * open dev mode
     */
    public HttpServ dev() {
        this.dev = true;

        // Add dev html page
        router.GET(HttpDevHtml.PAGE_PATH, new HttpDevHtml(this.name, router, ""));

        return this;
    }

    @Override
    public Hooks<SocketChannel> mountInitChannel() {
        return ch -> {
            ch.pipeline()
                    .addLast(new HttpServerCodec())
                    .addLast(new HttpObjectAggregator(1024 * 1024))
                    .addLast(new ChunkedWriteHandler())
                    // .addLast(new HttpServerExpectContinueHandler())
                    .addLast(new HttpServerHandler(router, ssl(), httpDataFactory));
        };
    }

    @Override
    protected HttpServ me() {
        return this;
    }

    @SneakyThrows
    @Override
    protected void onStartSuccess() {
        final var address = scheme() + "://" + getLocalHost().getHostAddress() + ":" + this.port;
        log.info("Server start Listening on:" + address);

        if (dev) {
            log.info("DEV mode open : " + address + HttpDevHtml.PAGE_PATH);
        }
    }

    @Override
    protected void init() {
        this.servOptions(ChannelOption.SO_BACKLOG, 1024);
        this.childOptions(ChannelOption.TCP_NODELAY, true);
        this.childOptions(ChannelOption.SO_KEEPALIVE, true);

        super.init();
    }
}