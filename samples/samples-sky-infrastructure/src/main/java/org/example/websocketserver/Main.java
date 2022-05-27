package org.example.websocketserver;

import http.HttpServer;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/26 16:01
 */
public class Main {

    public static void main(String[] args) {
        HttpServer.create().handle((req, resp) -> {
                    req.upgradeToWebSocket(ws -> {

                        System.out.println(ws.scheme());

                        ws.mountOpen(h -> {
                            ws.send("hello world");
                        });

                    });
                })
                .listen(8888)
                .dispose();
    }
}