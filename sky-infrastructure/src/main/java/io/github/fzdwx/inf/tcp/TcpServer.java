package io.github.fzdwx.inf.tcp;

import io.github.fzdwx.inf.ServInf;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/22 19:28
 */
public class TcpServer extends ServInf<TcpServer> {

    public TcpServer(final int port) {
        super(port);
    }

    @Override
    public Hooks<SocketChannel> registerInitChannel() {
        return sc -> {
            sc.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                @Override
                protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
                    System.out.println(msg);
                }
            });
        };
    }

    @Override
    protected TcpServer me() {
        return this;
    }
}