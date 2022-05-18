package sky.starter.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import sky.starter.ext.SkyBanner;

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

    public boolean enableDebug() {
        return this.sky.debug;
    }

    @Data
    public static class SkyHttpServer {

        static SkyHttpServer ins = new SkyHttpServer();

        /**
         * show sky banner.
         *
         * @see SkyBanner#print()
         */
        public Boolean banner = true;

        /**
         * path prefix
         */
        public String path = "sky";

        /**
         * use debug model.
         */
        public Boolean debug = false;
    }
}