package http;

import io.github.fzdwx.lambada.Lang;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 16:44
 */
class HttpServerTest {


    @Test
    void test_http() {
        final Router router = Router.router();
        new HttpServer((req, response) -> {
            req.upgradeToWebSocket().then(ws -> {
                ws.mountOpen((h) -> {
                    ws.send("hello");
                });
            });
        }).withGroup(0, 0).bind(8888);

        Lang.sleep(Duration.ofSeconds(1000000000L));
    }
}