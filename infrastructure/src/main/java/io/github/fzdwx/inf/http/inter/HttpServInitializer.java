package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.route.Router;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * http serv initializer.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 17:00
 */
public class HttpServInitializer extends ChannelInitializer<SocketChannel> {

    private final Router router;

    public HttpServInitializer(final Router router) {
        this.router = router;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(1024 * 1024))
                .addLast(new ChunkedWriteHandler())
                .addLast(new HttpServerExpectContinueHandler())
                .addLast(new HttpServerHandler(router));
    }
}