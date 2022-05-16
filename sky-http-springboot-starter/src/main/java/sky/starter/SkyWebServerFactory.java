package sky.starter;

import http.HttpServer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * //ServletWebServerFactoryConfiguration
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 21:02
 */
public class SkyWebServerFactory extends AbstractServletWebServerFactory implements ConfigurableWebServerFactory, ResourceLoaderAware {

    public SkyWebServerFactory() {
    }

    public SkyWebServerFactory(int port) {
        super(port);
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
        // todo
    }

    private SkyWebServer getSkyWebServer(final ServletContextInitializer[] initializers) {
        return new SkyWebServer(HttpServer.create(), getPort());
    }

}