package sky.starter.util;

import io.github.fzdwx.lambada.Console;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 9:55
 */
@Slf4j
public class SkyBanner {

    private final static String version = "0.10.6-dev";

    public static void print() {
        log.info(Console.banner() + Console.cyan("  [ Sky Starter ]  ") + Console.ANSI_RESET + "                             (v{})", version);
    }
}