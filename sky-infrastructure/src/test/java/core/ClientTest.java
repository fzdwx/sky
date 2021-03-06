package core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import org.junit.jupiter.api.Test;
import util.Netty;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/11 16:08
 */
class ClientTest {

    @Test
    void test2() {
        new Thread(() -> {
            new Client()
                    .withOptions(ChannelOption.TCP_NODELAY, true)
                    .childHandler(ch -> {
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

        }).start();
    }

    @Test
    void name() {
        System.out.println(Duration.ofSeconds(3).toMillis());
    }
}