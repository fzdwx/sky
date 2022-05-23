package util;

import cn.hutool.core.util.RandomUtil;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/22 12:24
 */
public class AvailablePort {

    private static final int MAX_PORT = 65535;
    private static final int MIN_PORT = 8000;
    private static final InetAddress local = getLocal();
    private static final ThreadLocalRandom random = RandomUtil.getRandom();

    public static Integer random() {
        return random(MIN_PORT, MAX_PORT);
    }

    public static Integer random(int min) {
        return random(min, MAX_PORT);
    }

    public static Integer random(int min, int max) {
        var maxRetryCount = max - min;
        final var bound = maxRetryCount;
        while (maxRetryCount > 0) {
            int port = random.nextInt(bound) + min;
            boolean isUsed = isLocalePortUsing(port);
            if (!isUsed) {
                return port;
            }
            --maxRetryCount;
        }
        return null;
    }

    public static boolean isLocalePortUsing(int port) {
        try (Socket socket = new Socket(local, port)) {
            return true;
        } catch (Exception e) {
            // 异常说明端口连接不上，端口能使用
        }
        return false;
    }

    @SneakyThrows
    private static InetAddress getLocal() {
        return InetAddress.getLocalHost();
    }
}