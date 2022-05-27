package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/27 11:33
 */
@RestController
public class HelloController {

    @GetMapping("hello")
    public String hello() {
        return "Hello";
    }
}