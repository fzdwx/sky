# Sky | Netty Based Transport Tool Kit.

🚀 Use sky you can quickly create an http service or websocket service

## Features

- [x] Http Server
- [x] Websocket Server
- [ ] Spring boot starter [in development](https://github.com/fzdwx/sky/tree/dev-springboot-starter)
- [ ] 😙give me some issue!

## Showcase

[click me](https://github.com/fzdwx/sky/blob/main/sky-http-springboot-starter/README.md)

### TCP Server

```java
 new Server()
        .withGroup(0,0)
        .withLog(LogLevel.INFO)
        .withInitChannel(ch->{
        // add your handler
        })
        .listen(8888)
        .dispose();
```

### HTTP Server

```java
HttpServer.create()
        .handle((request,response)->{
        response.json("hello world")
        })
        .listen(port)
        .dispose();
```

### WebSocket Server

```java
public class Test {

    void test() {
        HttpServer.create()
                .handle((request, response) -> {
                    request.upgradeToWebSocket(ws -> {
                        ws.mountOpen(h -> {
                            // client connect
                        });
                        ws.mountBinary(b -> {
                            // client send binary data
                        });
                        // ...
                    });
                })
                .listen(port)
                .dispose();
    }

}
```