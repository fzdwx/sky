package org.atomicode.fzdwx.springboottest.controller;

import io.github.fzdwx.inf.http.core.HttpServerRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 14:22
 */
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