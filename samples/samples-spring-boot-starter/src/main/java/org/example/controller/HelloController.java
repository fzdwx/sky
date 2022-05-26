package org.example.controller;

import http.HttpServerRequest;
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