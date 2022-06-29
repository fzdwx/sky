package sky.starter.util;

import io.github.fzdwx.lambada.Console;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 9:55
 */
@Slf4j
public class SkyBanner {

    private final static String version = "0.11.3.2";

    public static void print() {
        log.info(Console.banner() + Console.cyan("  [ Sky Web Server ]  ") + Console.ANSI_RESET + "                             (v{})", version);
    }
}