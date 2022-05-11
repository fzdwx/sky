package http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import http.ext.HttpHandler;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.http.ContentType;
import io.github.fzdwx.lambada.http.HttpMethod;
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
            Seq.range(100).onClose(() -> res.end("end"))
                    .forEach(i -> {
                        res.writeFlush(i + "\n");
                        Lang.sleep(Duration.ofMillis(100L));
                    });
            // System.out.println("end");
        }).GET("/1111", (req, res) -> {
            res.redirect("http://www.baidu.com");
        }).GET("/error", (req, resp) -> {
            throw new RuntimeException("json 序列化错误");
        });

        // 动态添加路由
        router.POST("/d", (req, res) -> {
            final JSONObject jsonObject = JSON.parseObject(req.readJsonString());

            final String path = jsonObject.getString("path");
            final String text = jsonObject.getString("text");
            final String method = jsonObject.getString("method");

            router.add(HttpMethod.of(method), path, (req1, res1) -> {
                res1.end(text);
            });
            res.end();
        });


        final int port = 8888;
        HttpServer.create().handle((req, response) -> {
            final Tuple2<HttpHandler, NvMap> t2 = router.match(req);
            if (t2.v1 != null) {
                t2.v1.handle(req, response);
                return;
            }

            response.notFound(req.toString());

        }).withLog(LogLevel.DEBUG).withGroup(0, 0).afterStart(h -> {
            System.out.println("http server start http://localhost:" + port);
        }).bind(port).dispose();

        // Lang.sleep(Duration.ofSeconds(1000000000L));
    }
}