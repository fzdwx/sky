package io.github.fzdwx.http;

import io.github.fzdwx.inf.ServInf;
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
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 17:45
 */
@Slf4j
public class HttpServ extends ServInf {

    public HttpServ(final int port) {
        super(port);
    }

    @Override
    public @NonNull ChannelInitializer<SocketChannel> addChildHandler() {
        return new HttpServInitializer();
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
        log.info("Server start Listening on: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + this.port);
    }

    public static void main(String[] args) {
        new HttpServ(8888).start();
    }
}