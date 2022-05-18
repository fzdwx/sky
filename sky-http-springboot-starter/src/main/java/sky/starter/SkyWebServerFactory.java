package sky.starter;

import http.HttpServer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import sky.starter.props.SkyHttpServerProps;

/**
 * //ServletWebServerFactoryConfiguration
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 21:02
 */
public class SkyWebServerFactory extends AbstractServletWebServerFactory implements ConfigurableWebServerFactory, ResourceLoaderAware {

    private SkyHttpServerProps skyHttpServerProps;
    private HttpServer httpServer;

    public SkyWebServerFactory() {
        this.httpServer = HttpServer.create();
    }

    public SkyWebServerFactory(int port, HttpServer httpServer, SkyHttpServerProps skyHttpServerProps) {
        super(port);
        this.httpServer = httpServer;
        this.skyHttpServerProps = skyHttpServerProps;
    }

    public SkyWebServerFactory(String contextPath, int port) {
        super(contextPath, port);
    }

    @Override
    public WebServer getWebServer(final ServletContextInitializer... initializers) {
        return getSkyWebServer(initializers);
    }

    @Override
    public void setResourceLoader(final ResourceLoader resourceLoader) {
        // todo e.g static file
    }

    private SkyWebServer getSkyWebServer(final ServletContextInitializer[] initializers) {
        return new SkyWebServer(httpServer, getPort(), skyHttpServerProps);
    }

}