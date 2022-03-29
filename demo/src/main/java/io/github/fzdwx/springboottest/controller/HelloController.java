package io.github.fzdwx.springboottest.controller;

import io.github.fzdwx.RequestMounter;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import io.github.fzdwx.inf.route.Router;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 14:22
 */
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