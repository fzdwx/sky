package sky.starter;

import http.HttpServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 21:00
 */
public class SkyWebServer implements WebServer {

    private final HttpServer server;

    private final int port;

    public SkyWebServer(final HttpServer server, final int port) {
        this.server = server;
        this.port = port;
    }

    @Override
    public void start() throws WebServerException {
        server.listen(getPort());
    }

    @Override
    public void stop() throws WebServerException {
        // server.dispose();
    }

    @Override
    public int getPort() {
        return port;
    }
}