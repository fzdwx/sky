package sky;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import sky.starter.SkyDispatcherServletPath;
import sky.starter.SkyServletContext;
import sky.starter.SkyWebServer;
import sky.starter.SkyWebServerFactory;
import sky.starter.ext.SkyBanner;
import sky.starter.props.SkyHttpServerProps;

import javax.servlet.ServletContext;

/**
 * sky http server auto configuration.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 22:25
 */
@Configuration
@ConditionalOnClass({SkyWebServer.class, SkyWebServerFactory.class})
@EnableConfigurationProperties(SkyHttpServerProps.class)
public class SkyHttpServerAutoConfiguration {

    private SkyHttpServerProps skyHttpServerProps;

    public SkyHttpServerAutoConfiguration(final SkyHttpServerProps skyHttpServerProps) {
        this.skyHttpServerProps = skyHttpServerProps;

        showBanner();
    }

    @Bean
    AbstractServletWebServerFactory abstractServletWebServerFactory(WebMvcConfigurationSupport webMvcConfigurationSupport) {
        webMvcConfigurationSupport.setServletContext(servletContext());
        return new SkyWebServerFactory(skyHttpServerProps.port);
    }

    @Bean
    @Primary
    DispatcherServletPath dispatcherServletPath() {
        return new SkyDispatcherServletPath(skyHttpServerProps.sky.path);
    }

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