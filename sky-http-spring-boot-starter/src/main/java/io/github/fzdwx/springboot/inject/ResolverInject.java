package io.github.fzdwx.springboot.inject;

import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.resolver.BodyResolver;
import io.github.fzdwx.resolver.HttpRequestResolver;
import io.github.fzdwx.resolver.HttpResponseResolver;
import io.github.fzdwx.resolver.ParamResolver;
import io.github.fzdwx.resolver.Resolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

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

    @Bean
    public BodyResolver bodyResolver() {
        return new BodyResolver();
    }

    @Bean
    public HttpRequestResolver httpRequestResolver() {
        return new HttpRequestResolver();
    }

    @Bean
    public HttpResponseResolver httpResponseResolver() {
        return new HttpResponseResolver();
    }

    @Bean
    public ResolverMapping resolverMapping(@Autowired List<Resolver> resolvers) {
        return new ResolverMapping(resolvers);
    }

    public static class ResolverMapping {

        private final Map<? extends Class<?>, Resolver> resolverMap;

        public ResolverMapping(final List<Resolver> resolvers) {
            resolverMap = Seq.of(resolvers).toMap(Resolver::getType);
        }

        public Resolver get(Class<?> annotationType) {
            return this.resolverMap.get(annotationType);
        }
    }
}