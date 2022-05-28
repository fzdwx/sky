package sky.starter.bean;

import core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import sky.starter.props.SkyHttpServerProps;

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
    private final SkyHttpServerProps skyHttpServerProps;

    public SkyWebServer(final HttpServer server,
                        final SkyHttpServerProps skyHttpServerProps) {
        this.port = skyHttpServerProps.getPort();
        this.server = server;
        this.skyHttpServerProps = skyHttpServerProps;
    }

    @Override
    public void start() throws WebServerException {
        if (skyHttpServerProps.enableDebug()) {
            log.info(DEBUG_PREFIX + "start SkyHttpServer");
        }
        server.listen(getPort());
    }

    @Override
    public void stop() throws WebServerException {
        if (skyHttpServerProps.enableDebug()) {
            log.info(DEBUG_PREFIX + "stop SkyHttpServer");
        }
        server.shutdown();
    }

    @Override
    public int getPort() {
        return port;
    }
}