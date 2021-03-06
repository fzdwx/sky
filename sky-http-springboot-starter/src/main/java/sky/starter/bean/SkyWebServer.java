package sky.starter.bean;

import core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import sky.starter.props.SkyWebServerProps;

import static sky.starter.util.Utils.DEBUG_PREFIX;

/**
 * Sky webServer
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 21:00
 */
@Slf4j
public class SkyWebServer implements WebServer {

    private final int port;
    private final HttpServer server;
    private final SkyWebServerProps skyWebServerProps;

    public SkyWebServer(final HttpServer server, final SkyWebServerProps skyWebServerProps) {
        this.port = skyWebServerProps.getPort();
        this.server = server;
        this.skyWebServerProps = skyWebServerProps;
    }

    @Override
    public void start() throws WebServerException {
        if (skyWebServerProps.enableDebug()) {
            log.info(DEBUG_PREFIX + "start SkyHttpServer");
        }

        server.onFailure(f -> {
            log.error(util.Utils.PREFIX + "start SkyHttpServer failure", f);
            System.exit(1);
        }).listen(getPort());
    }

    @Override
    public void stop() throws WebServerException {
        if (skyWebServerProps.enableDebug()) {
            log.info(DEBUG_PREFIX + "stop SkyHttpServer");
        }
        server.shutdown();
    }

    @Override
    public int getPort() {
        return port;
    }
}