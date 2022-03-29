package io.github.fzdwx.springboot.inject;

import io.github.fzdwx.resolver.ParamResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 16:45
 */
@Configuration
public class ResolverInject {

    @Bean
    public ParamResolver paramResolver() {
        return new ParamResolver();
    }
}