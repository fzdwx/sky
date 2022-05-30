package http;

import core.http.HttpServer;
import core.http.handler.StaticFileHandler;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/29 20:39
 */
public class FileServerTest {

    @Test
    void test_run() {
        HttpServer.create()
                .requestHandler(StaticFileHandler.create("src/test"))
                .listen(8888)
                .dispose();
    }
}