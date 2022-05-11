package core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/11 16:08
 */
class ClientTest {

    @Test
    void test() {
        final Client c = new Client()
                .withOptions(ChannelOption.TCP_NODELAY, true)
                .withInitChannel(ch -> {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                            ctx.writeAndFlush(Netty.wrap(ctx.alloc(),"hello"));
                        }

                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) {
                            ctx.flush();
                        }

                        @Override
                        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                            System.out.println("client receive: " + msg);
                        }
                    });
                })
                .start("192.168.1.248", 8888);

        c.dispose();
    }
}