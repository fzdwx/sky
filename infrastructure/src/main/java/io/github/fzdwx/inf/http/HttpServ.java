package io.github.fzdwx.inf.http;

import io.github.fzdwx.inf.ServInf;
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

import java.net.InetAddress;

/**
 * http serv.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 11:25
 */
@Slf4j
public class HttpServ extends ServInf {

    private final Router router;
    private final boolean dev;

    /**
     * http server
     *
     * @param port   the listening port
     * @param router router
     */
    public HttpServ(final int port, final Router router) {
        super(port);
        this.router = router;
        this.dev = false;
    }

    public HttpServ(final int port, final Router router, final boolean dev) {
        super(port);
        this.router = router;
        this.dev = dev;
    }

    @SneakyThrows
    @Override
    protected void onStart(final Future<? super Void> f) {
        final var address = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + this.port;
        log.info("Server start Listening on:" + address);

        if (dev) {
            router.GET("/dev", new HttpDevHtml(router));
            log.info("DEV mode open : " + address + "/dev");
        }
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
}