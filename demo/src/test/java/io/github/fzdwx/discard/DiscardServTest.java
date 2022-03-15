package io.github.fzdwx.discard;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/15 21:59
 */
class DiscardServTest {

    @org.junit.jupiter.api.Test
    void run() throws InterruptedException {
        final var discardServ = new DiscardServ(8888);

        discardServ.run();
    }
}