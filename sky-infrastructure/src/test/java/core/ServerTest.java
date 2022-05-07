package core;

import io.github.fzdwx.lambada.Lang;
import io.netty.handler.logging.LogLevel;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 15:44
 */
class ServerTest {


    @Test
    void test_server() {
        new Server()
                .withGroup(0, 0)
                .withLog(LogLevel.INFO)
                .bind(8888)
                .withInitChannel(ch -> {
                    // init socket channel
                })
        ;

        Lang.sleep(Duration.ofSeconds(10000000000L));
    }
}