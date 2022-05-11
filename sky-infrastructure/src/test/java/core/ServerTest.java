package core;

import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.Seq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.logging.LogLevel;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 15:44
 */
class ServerTest {


    @Test
    void test_server() {
        final Server s = new Server()
                .withGroup(0, 0)
                .withLog(LogLevel.INFO)
                .start(8888)
                .withInitChannel(ch -> {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                            Seq.range(100)
                                    .forEach(i -> {
                                        ctx.writeAndFlush(i);
                                        Lang.sleep(Duration.ofMillis(100));
                                    });
                        }
                    });
                });

        s.dispose();
    }
}