package org.example.controller;

import core.http.ext.HttpServerRequest;
import core.http.response.HttpResponse;
import core.http.response.JsonMapHttpResponse;
import core.http.response.JsonObjectHttpResponse;
import io.github.fzdwx.lambada.Collections;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/23 20:15
 */
@RestController
public class HelloController {

    @GetMapping("/hello/{name}")
    public Object hello(@PathVariable final String name) {
        return Collections.map("hello", name);
    }

    @GetMapping("test1")
    public JsonObjectHttpResponse test1() {
        return HttpResponse.json("hello world");
    }

    @GetMapping("test2")
    public JsonObjectHttpResponse test2() {
        return HttpResponse.json(() -> {
            System.out.println("test2");
        });
    }

    @GetMapping("test3")
    public JsonMapHttpResponse test3() {
        return HttpResponse.json("token", 123);
    }

    @GetMapping("test4")
    public HttpResponse<?> test4() {
        return HttpResponse.ok(() -> {
            System.out.println("test4");
        });
    }

    @GetMapping("test5")
    public HttpResponse<?> test5() {
        return HttpResponse.ok(() -> {
            return "test5";
        });
    }

    @GetMapping("test6")
    public HttpResponse<?> test6() {
        return HttpResponse.json(() -> {
            return "test6";
        });
    }

    @GetMapping("test7")
    public HttpResponse<?> test7() {
        return HttpResponse.ok().body("test7");
    }

    @GetMapping("/connect")
    public void connect(HttpServerRequest request, @RequestParam String name) {
        request.upgradeToWebSocket(ws -> {

            ws.attr("key", "like");

            ws.mountOpen(h -> {
                ws.send("hello " + name);

                final Object key = ws.attr("key");
                System.out.println(key);
            });
        });
    }
}