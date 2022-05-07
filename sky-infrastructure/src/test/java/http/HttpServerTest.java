package http;

import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.internal.Tuple2;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.handler.logging.LogLevel;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 16:44
 */
class HttpServerTest {


    @Test
    void test_http() {
        final Router router = Router.router().GET("/ws", (req, res) -> {
            req.upgradeToWebSocket().then(ws -> {
                ws.mountOpen((h) -> {
                    ws.send("hello");
                });
            });
        }).GET("/post", (req, res) -> {
            res.contentType(ContentType.STREAM_JSON);
            res.writeFlush("123123123\n");
            Lang.sleep(Duration.ofSeconds(1L));
            res.writeFlush("aaaaaaaaaaa\n");
            Lang.sleep(Duration.ofSeconds(1L));
            res.writeFlush("bbbbbbbbbb\n");
            Lang.sleep(Duration.ofSeconds(1L));
            res.end("ccccccccc\n");
        }).GET("/event", (req, res) -> {
            res.contentType(ContentType.EVENT_STREAM);
            res.writeFlush("event1\n");
            Lang.sleep(Duration.ofSeconds(1L));
            res.writeFlush("event2\n");
            Lang.sleep(Duration.ofSeconds(1L));
            res.writeFlush("event3\n");
            Lang.sleep(Duration.ofSeconds(1L));
            res.end("event4\n");
        }).GET("/1111", (req, res) -> {
            res.redirect("http://www.baidu.com");
        });

        HttpServer.create()
                .handle((req, response) -> {
                    final Tuple2<HttpHandler, NvMap> t2 = router.match(req);
                    if (t2.v1 != null) {
                        t2.v1.handle(req, response);
                        return;
                    }

                    response.notFound(req.toString());

                })
                .withLog(LogLevel.INFO)
                .withGroup(0, 0)
                .bind(8888)
                .dispose();

        // Lang.sleep(Duration.ofSeconds(1000000000L));
    }
}