package sky.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 22:47
 */
@Data
@ConfigurationProperties(prefix = "server")
public class SkyHttpServerProps {

    /**
     * http server port
     */
    public int port = 9999;

    /**
     * sky http server config
     */
    public SkyHttpServer sky = SkyHttpServer.ins;

    @Data
    public static class SkyHttpServer {

         static SkyHttpServer ins = new SkyHttpServer();

        /**
         * path prefix
         */
        public String path = "sky";
    }
}