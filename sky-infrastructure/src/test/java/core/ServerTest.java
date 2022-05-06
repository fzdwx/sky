package core;

import io.github.fzdwx.inf.http.core.HttpServerHandler;
import http.Router;
import io.github.fzdwx.lambada.Lang;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 15:44
 */
class ServerTest {


    @Test
    void test_server() {
        new Server()
                .withGroup(0, 0)
                .withLog(LogLevel.INFO)
                .bind(8888)
                .withInitChannel(ch -> {
                    ch.pipeline()
                            .addLast(new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(1024 * 1024))
                            .addLast(new ChunkedWriteHandler())
                            .addLast(new HttpServerExpectContinueHandler())
                            .addLast(new HttpServerHandler(Router.router().GET("/", (req, resp) -> {
                                resp.end("hello world");
                            }), false, null));
                })
        ;

        Lang.sleep(Duration.ofSeconds(10000000000L));
    }
}