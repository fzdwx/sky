package io.github.fzdwx;

import io.github.fzdwx.discard.DiscardServ;
import io.github.fzdwx.echo.EchoServ;
import io.github.fzdwx.time.TimeServ;
import io.github.fzdwx.time.client.TimeClient;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/15 21:59
 */
class ServTest {

    @Test
    void test_echo() throws InterruptedException {
        new EchoServ(8888).run();
    }

    @Test
    void run() throws InterruptedException {
        final var discardServ = new DiscardServ(8888);

        discardServ.run();
    }

    @Test
    void test_time() throws InterruptedException {
        new TimeServ(8888).run();
    }

    @Test
    void test_time_client() throws InterruptedException {
        new TimeClient(8888).run();
    }
}