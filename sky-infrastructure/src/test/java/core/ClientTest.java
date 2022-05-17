package core;

import io.github.fzdwx.lambada.Threads;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/11 16:08
 */
class ClientTest {

    @Test
    void test2() {
        new Thread(() -> {
            final Client c = new Client()
                    .withOptions(ChannelOption.TCP_NODELAY, true)
                    .withInitChannel(ch -> {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                            @Override
                            public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                                ctx.writeAndFlush(Netty.wrap(ctx.alloc(), "hello"));
                            }

                            @Override
                            public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                                System.out.println("client receive: " + Netty.read((ByteBuf) msg));
                            }
                        });
                    })
                    .withEnableAutoReconnect(Duration.ofSeconds(10))
                    .listen("localhost", 8888);

            c.dispose();
        }).start();

        Threads.sleep(Duration.ofDays(5));
    }

    @Test
    void name() {
        System.out.println(Duration.ofSeconds(3).toMillis());
    }
}