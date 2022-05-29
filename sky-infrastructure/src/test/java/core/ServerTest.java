package core;

import io.netty.handler.logging.LogLevel;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 15:44
 */
class ServerTest {

    @Test
    void test_server() {
        new Server()
                .log(LogLevel.INFO)
                .childHandler(ch -> {
                    // add your handler
                })
                .listen(8888)
                .dispose();
    }
}