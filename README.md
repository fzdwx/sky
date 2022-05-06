# Netty Showcase

使用Netty写的一些小demo

## features

- [x] Http Server
- [x] Websocket Server
- [ ] 大文件发送 继续优化
- [ ] todo... need your idea !

## Show case

```java
class HttpServerTest {


    @Test
    void test_http() {
        final Router router = Router.router()
                .GET("/ws", (req, res) -> {

                    // expose websocket endpoint
                    req.upgradeToWebSocket().then(ws -> {
                        ws.mountOpen((h) -> {
                            ws.send("hello");
                        });
                    });
                })
                // event stream
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
        new HttpServer((req, response) -> {

            final Tuple2<HttpHandler, NvMap> t2 = router.match(req);
            if (t2.v1 != null) {
                t2.v1.handle(req, response);
                return;
            }

            response.sendNotFound();

        }).withGroup(0, 0).bind(8888);

        Lang.sleep(Duration.ofSeconds(1000000000L));
    }
}
```