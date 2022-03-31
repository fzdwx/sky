package io.github.fzdwx.springboottest.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/31 11:09
 */
@RestController
@RequestMapping("/hello")
public class Rest123123 {

    @PostMapping("world")
    public void word() {
        System.out.println("hello world");
    }
}