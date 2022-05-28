package core;

import io.github.fzdwx.lambada.Console;
import org.jline.reader.LineReader;

import java.util.Objects;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/25 17:31
 */
public class TestDisbind {

    public static void main(String[] args) {
        final Server listen = new Server()
                .listen(10010);
        new Server()
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