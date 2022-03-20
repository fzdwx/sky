package io.github.fzdwx.inf.http;

import io.github.fzdwx.inf.ServInf;
import io.github.fzdwx.inf.http.core.HttpServerHandler;
import io.github.fzdwx.inf.http.inter.HttpDevHtml;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.github.fzdwx.lambada.Lang.todo;
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
    private boolean ssl;

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

    /**
     * open dev mode
     */
    public HttpServ dev() {
        this.dev = true;

        // Add dev html page
        router.GET(HttpDevHtml.PAGE_PATH, new HttpDevHtml(this.name, router));

        return this;
    }

    public HttpServ ssl() {
        return todo();
    }

    @Override
    public Hooks<SocketChannel> registerInitChannel() {
        return ch -> {
            ch.pipeline()
                    .addLast(new HttpServerCodec())
                    .addLast(new HttpObjectAggregator(1024 * 1024))
                    .addLast(new ChunkedWriteHandler())
                    .addLast(new HttpServerExpectContinueHandler())
                    .addLast(new HttpServerHandler(router));
        };
    }

    @Override
    public @NonNull Class<? extends ServerChannel> serverChannelClass() {
        return NioServerSocketChannel.class;
    }

    @SneakyThrows
    @Override
    protected void onStartSuccess() {
        final var address = (ssl ? "https" : "http") + "://" + getLocalHost().getHostAddress() + ":" + this.port;
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

    @Override
    protected HttpServ me() {
        return this;
    }
}