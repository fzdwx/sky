# Sky infrastructure

It encapsulates a layer on netty, which is easier to use

## Install

```xml
<dependency>
  <groupId>io.github.fzdwx</groupId>
  <artifactId>sky-infrastructure</artifactId>
  <version>0.11.2</version>
</dependency>
```

## Showcase


### 1.TCP Server

```java
public class Test {

    void test() {
        new Server()
                .withGroup(0, 0)
                .withLog(LogLevel.INFO)
                .withInitChannel(ch -> {
                    // add your handler
                })
                .listen(8888)
                .dispose();
    }
}
```

### 2.HTTP Server

```java
public class Test {

    void test() {
        HttpServer.create()
                .handle((request, response) -> {
                    response.json("hello world");
                })
                .listen(port)
                .dispose();
    }

}
```

### 3.WebSocket Server

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