package sky.starter.bean;

import core.http.HttpServer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import sky.starter.props.SkyWebServerProps;

/**
 * Sky webServer builder.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 21:02
 */
public class SkyWebServerFactory extends AbstractServletWebServerFactory implements ConfigurableWebServerFactory, ResourceLoaderAware {

    private final SkyDispatchHandler skyDispatchHandler;
    private SkyWebServerProps skyWebServerProps;
    private HttpServer httpServer;

    public SkyWebServerFactory(final HttpServer httpServer,
                               final SkyWebServerProps skyWebServerProps,
                               final SkyDispatchHandler skyDispatchHandler) {
        super(skyWebServerProps.getPort());
        this.httpServer = httpServer;
        this.skyWebServerProps = skyWebServerProps;
        this.skyDispatchHandler = skyDispatchHandler;
    }

    @EventListener
    public void test2(RequestResultHandlerContainer event) {
        System.out.println(event);
    }

    @EventListener
    public void test3(RequestArgumentResolverContainer event) {
        System.out.println(event);
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
        // TODO: 2022/5/18 customize exception handler
        httpServer.requestHandler(skyDispatchHandler);
        return new SkyWebServer(httpServer, skyWebServerProps);
    }

}