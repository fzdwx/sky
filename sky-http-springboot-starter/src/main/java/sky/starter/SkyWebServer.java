package sky.starter;

import http.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import sky.starter.props.SkyHttpServerProps;

import static sky.starter.ext.Utils.DEBUG_PREFIX;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 21:00
 */
@Slf4j
public class SkyWebServer implements WebServer {

    private final HttpServer server;
    private final int port;
    private final SkyHttpServerProps skyHttpServerProps;


    public SkyWebServer(final HttpServer server, final int port, final SkyHttpServerProps skyHttpServerProps) {
        this.server = server;
        this.port = port;
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
        server.close();
    }

    @Override
    public int getPort() {
        return port;
    }
}