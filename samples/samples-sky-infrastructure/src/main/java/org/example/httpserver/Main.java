package org.example.httpserver;

import core.http.HttpServer;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/26 15:46
 */
public class Main {

    public static void main(String[] args) {
        HttpServer.create().handle((req, resp) -> {
                    resp.end("hello world");
                })
                .listen(8888)
                .dispose();
    }
}