package org.example.fileserver;

import core.http.HttpServer;
import core.http.handler.StaticFileHandler;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/30 16:31
 */
public class Main {

    public static void main(String[] args) {
        HttpServer.create().requestHandler(StaticFileHandler.create())
                .listen(80)
                .dispose();
    }
}