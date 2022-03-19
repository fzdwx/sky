package io.github.fzdwx.inf.http;

import io.github.fzdwx.inf.ServInf;
import io.github.fzdwx.inf.http.core.HttpServInitializer;
import io.github.fzdwx.inf.http.inter.HttpDevHtml;
import io.github.fzdwx.inf.route.Router;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.NonNull;
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
public class HttpServ extends ServInf {

    private final Router router;
    private boolean dev;

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

    @Override
    public HttpServ name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public @NonNull ChannelInitializer<SocketChannel> addChildHandler() {
        return new HttpServInitializer(router);
    }

    @Override
    public @NonNull Class<? extends ServerChannel> serverChannelClass() {
        return NioServerSocketChannel.class;
    }

    @Override
    public void addServOptions() {
        this.serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        this.serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        this.serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @SneakyThrows
    @Override
    protected void onStart(final Future<? super Void> f) {
        final var address = "http://" + getLocalHost().getHostAddress() + ":" + this.port;
        log.info("Server start Listening on:" + address);

        if (dev) {
            log.info("DEV mode open : " + address + HttpDevHtml.PAGE_PATH);
        }
    }
}