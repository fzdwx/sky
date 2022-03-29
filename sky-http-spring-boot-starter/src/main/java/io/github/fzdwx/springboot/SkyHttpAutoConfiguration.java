package io.github.fzdwx.springboot;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.HttpServ;
import io.github.fzdwx.inf.route.Router;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 14:09
 */
public class SkyHttpAutoConfiguration {

    @Bean
    @Order(1)
    @ConditionalOnMissingBean
    public Router router() {
        return Router.router();
    }

    @Bean
    @Order(Integer.MAX_VALUE - 10)
    public Inject inject(@Autowired Router router) {
        return new Inject(router);
    }

    @Bean
    @Order
    public HttpServ httpServ(
            @Value("${server.port}") int port,
            @Autowired Router router
    ) throws InterruptedException {
        final HttpServ dev = Netty.HTTP(port, router)
                .dev();
        dev.bind().sync();
        return dev;
    }
}