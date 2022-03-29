# sky-http-spring-boot-starter

http server spring boot starter

## Usage
1. impl RequestMounter.
2. add your http request handle.

```java
@Component
public class HelloController implements RequestMounter {

    @Override
    public void mount(final Router router) {
        router.GET("/test", this::test);
    }

    public void test(HttpServerRequest request, HttpServerResponse response) {
        response.end("hello world");
    }
}
```

## Todo
- [ ] support more config.