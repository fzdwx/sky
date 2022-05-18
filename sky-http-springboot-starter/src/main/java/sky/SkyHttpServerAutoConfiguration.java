package sky;

import http.HttpServer;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.http.Router;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import sky.starter.DispatchHandler;
import sky.starter.SkyHandlerMappingContainer;
import sky.starter.SkyWebServer;
import sky.starter.SkyWebServerFactory;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.HandlerMappingContainer;
import sky.starter.ext.RequestParamResolver;
import sky.starter.ext.RequestResultHandler;
import sky.starter.util.SkyBanner;
import sky.starter.props.SkyHttpServerProps;
import sky.starter.unsupport.SkyDispatcherServletPath;
import sky.starter.unsupport.SkyServletContext;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * sky http server auto configuration.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 22:25
 */
@Configuration
@ConditionalOnClass({SkyWebServer.class, SkyWebServerFactory.class})
@EnableConfigurationProperties(SkyHttpServerProps.class)
@Import({RequestResultHandlerInject.class, RequestParamResolverInject.class})
public class SkyHttpServerAutoConfiguration {

    private final SkyHttpServerProps skyHttpServerProps;

    private final List<RequestResultHandler> resultHandlers;
    private final List<RequestParamResolver> paramResolvers;

    public SkyHttpServerAutoConfiguration(final SkyHttpServerProps skyHttpServerProps,
                                          final List<RequestResultHandler> resultHandlers,
                                          final List<RequestParamResolver> paramResolvers) {
        this.skyHttpServerProps = skyHttpServerProps;
        this.resultHandlers = Seq.sort(resultHandlers, RequestResultHandler.sort);
        this.paramResolvers = paramResolvers;

        showBanner();
    }

    /**
     * sky web server factory.
     */
    @Bean
    @ConditionalOnMissingBean
    SkyWebServerFactory SkyWebServerFactory(
            WebMvcConfigurationSupport webMvcConfigurationSupport,
            HttpServer httpServer,
            DispatchHandler dispatchHandler) {

        webMvcConfigurationSupport.setServletContext(servletContext());

        return new SkyWebServerFactory(
                httpServer,
                skyHttpServerProps,
                dispatchHandler);
    }

    /**
     * handler mapping container,collect all handler mapping.
     */
    @Bean
    @ConditionalOnMissingBean
    HandlerMappingContainer<?> container(Router<SkyRouteDefinition> router) {
        return new SkyHandlerMappingContainer(skyHttpServerProps, router);
    }

    /**
     * dispatch handler.
     *
     * @param router router
     * @return {@link DispatchHandler }
     */
    @Bean
    @ConditionalOnMissingBean
    DispatchHandler dispatchHandler(
            Router<SkyRouteDefinition> router) {
        return new DispatchHandler(router, resultHandlers, paramResolvers);
    }

    /**
     * router.
     * <p>
     * Save the processor corresponding to the route.
     *
     * @return {@link Router }<{@link SkyRouteDefinition }>
     */
    @Bean
    @ConditionalOnMissingBean
    Router<SkyRouteDefinition> router() {
        return Router.router();
    }

    /**
     * http server
     *
     * @return {@link HttpServer }
     */
    @Bean
    @ConditionalOnMissingBean
    HttpServer httpServer() {
        return HttpServer.create();
    }

    /**
     * not support.
     */
    @Bean
    @Primary
    DispatcherServletPath dispatcherServletPath() {
        return new SkyDispatcherServletPath(skyHttpServerProps.sky.path);
    }

    /**
     * not support.
     */
    @Bean
    @Primary
    ServletContext servletContext() {
        return new SkyServletContext(skyHttpServerProps.sky.path);
    }

    private void showBanner() {
        if (skyHttpServerProps.sky.banner) {
            SkyBanner.print();
        }
    }
}