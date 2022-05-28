package sky.starter.bean;

import core.http.HttpServer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import sky.starter.props.SkyHttpServerProps;

/**
 * Sky webServer builder.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 21:02
 */
public class SkyWebServerFactory extends AbstractServletWebServerFactory implements ConfigurableWebServerFactory, ResourceLoaderAware {

    private final DispatchHandler dispatchHandler;
    private SkyHttpServerProps skyHttpServerProps;
    private HttpServer httpServer;

    public SkyWebServerFactory(final HttpServer httpServer,
                               final SkyHttpServerProps skyHttpServerProps,
                               final DispatchHandler dispatchHandler) {
        super(skyHttpServerProps.getPort());
        this.httpServer = httpServer;
        this.skyHttpServerProps = skyHttpServerProps;
        this.dispatchHandler = dispatchHandler;
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
        httpServer.handle(dispatchHandler);
        return new SkyWebServer(httpServer, skyHttpServerProps);
    }

}