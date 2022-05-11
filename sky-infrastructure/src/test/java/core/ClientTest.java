package core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/11 16:08
 */
class ClientTest {

    @Test
    void test() {
        final Client c = new Client()
                .start("localhost", 8888)
                .withInitChannel(ch -> {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                            System.out.println("client receive: " + msg);
                        }
                    });
                });

        c.dispose();
    }
}