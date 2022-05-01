package io.github.fzdwx.inf.tcp;

import io.github.fzdwx.inf.core.ServInf;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/22 19:28
 */
public class TcpServer extends ServInf<TcpServer> {

    public TcpServer(final int port) {
        super(port);
    }

    public static TcpServer create() {
        return new TcpServer(8888);
    }

    @Override
    public Hooks<SocketChannel> mountInitChannel() {
        return sc -> {
            sc.pipeline()
                    .addLast(new StringDecoder())
                    .addLast(new StringEncoder())
                    .addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                            System.out.println(msg);
                        }
                    });
        };
    }

    @Override
    protected TcpServer me() {
        return this;
    }

    @Override
    protected void init() {
        this.servOptions(ChannelOption.SO_BACKLOG, 1024);
        this.childOptions(ChannelOption.TCP_NODELAY, true);
        this.childOptions(ChannelOption.SO_KEEPALIVE, true);
        super.init();
    }
}