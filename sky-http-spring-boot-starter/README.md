# sky-http-spring-boot-starter

http server spring boot starter

## Usage

### functional style

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

### Spring boot style

you can choose to inject `HttpServerRequest` or `HttpServerReponse` to implement some more personalized functionsn

```java

@Controller
@RequestMapping("/hello")
public class HelloController {

    @PostMapping("hello")
    public void hello() {
        System.out.println("hello");
    }

    @GetMapping("hello")
    public void hello2(HttpServerRequest request, @RequestParam("name") String name) {
        System.out.println(request);
        System.out.println(Thread.currentThread().getName());
        System.out.println("name = " + name);
        System.out.println("hello2");
    }

    @PostMapping("/test")
    public void test() {
        System.out.println("test");
    }

    @GetMapping("getAutoEnd")
    public String getAutoEnd() {
        return "autoEnd";
    }
}
```

## Todo

- [ ] support more config.
- [ ] add request uri parse.
- [ ] add request body parse.
- [ ] add request header parse.
- [ ] more response value handler.