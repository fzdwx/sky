# Sky http springboot starter


Used to replace spring boot web starter,And use sky to provide http service

## Features

https://github.com/fzdwx/sky/issues/11

## Not Support
1. Servlet
2. Filter(会考虑一个替代的)

## Showcase

```xml
<dependency>
  <groupId>io.github.fzdwx</groupId>
  <artifactId>sky-http-springboot-starter</artifactId>
  <version>0.10.5</version>
</dependency>
```

```java
import http.HttpServerRequest;

@SpringBootApplication
@RestController
public class BurstServerApplication {

    public static void main(String[] args) {
        final ConfigurableApplicationContext run = SpringApplication.run(BurstServerApplication.class);
    }

    @GetMapping("hello")
    public String hello(@RequestParam String name) {
        return "Hello " + name;
    }

    @GetMapping("connect")
    public void connect(@RequestParam String name, HttpServerRequest request) {
        request.upgradeToWebSocket(ws->{
            ws.mountOpen(h -> {
                ws.send("Hello " + name); 
            });
        });
    }
}
```