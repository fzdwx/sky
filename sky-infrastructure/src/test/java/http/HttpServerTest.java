package http;

import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.internal.Tuple2;
import io.github.fzdwx.lambada.lang.NvMap;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 16:44
 */
class HttpServerTest {


    @Test
    void test_http() {
        final Router router = Router.router()
                .GET("/ws", (req, res) -> {
                    req.upgradeToWebSocket().then(ws -> {
                        ws.mountOpen((h) -> {
                            ws.send("hello");
                        });
                    });
                })
                .GET("/post", (req, res) -> {
                    res.header("Content-Type", ContentType.STREAM_JSON);
                    res.writeFlush("123123123\n");
                    Lang.sleep(Duration.ofSeconds(1L));
                    res.writeFlush("aaaaaaaaaaa\n");
                    Lang.sleep(Duration.ofSeconds(1L));
                    res.writeFlush("bbbbbbbbbb\n");
                    Lang.sleep(Duration.ofSeconds(1L));
                    res.end("ccccccccc\n");
                });
        new HttpServer()
                .handler((req, response) -> {

                    final Tuple2<HttpHandler, NvMap> t2 = router.match(req);
                    if (t2.v1 != null) {
                        t2.v1.handle(req, response);
                        return;
                    }

                    response.sendNotFound();

                })
                .withGroup(0, 0).bind(8888);

        Lang.sleep(Duration.ofSeconds(1000000000L));
    }
}