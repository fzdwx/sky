package core;

import io.github.fzdwx.lambada.Console;
import io.netty.channel.nio.NioEventLoopGroup;
import org.jline.reader.LineReader;

import java.util.Objects;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/25 17:31
 */
public class TestDisbind {

    public static void main(String[] args) {
        final NioEventLoopGroup boss = new NioEventLoopGroup();
        final NioEventLoopGroup work = new NioEventLoopGroup();
        final Server listen = new Server()
                .withGroup(boss, work)
                .listen(10010);
        new Server()
                .withGroup(boss, work)
                .listen(7777);

        final LineReader lineReader = Console.defaultLineReader();
        while (true) {
            final String s = lineReader.readLine(">");
            if (Objects.equals(s, "kill")) {
                listen.close().syncUninterruptibly().addListener(f -> {
                    if (f.isSuccess()) {
                        System.out.println("kill success");
                    }
                });
            }
        }
    }
}