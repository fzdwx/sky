package io.github.fzdwx.tcp;

import io.github.fzdwx.inf.tcp.TcpServer;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/22 21:14
 */
public class TcpTest {

    public static void main(String[] args) {
        TcpServer.create()
                .name("my server")
                .port(8080)
                .host("localhost")
                .bind();
    }
}