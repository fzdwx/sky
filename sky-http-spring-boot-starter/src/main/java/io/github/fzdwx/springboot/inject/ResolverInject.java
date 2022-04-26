package io.github.fzdwx.springboot.inject;

import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.resolver.BodyResolver;
import io.github.fzdwx.resolver.HttpRequestResolver;
import io.github.fzdwx.resolver.HttpResponseResolver;
import io.github.fzdwx.resolver.ParamResolver;
import io.github.fzdwx.resolver.PathVariableResolver;
import io.github.fzdwx.resolver.Resolver;
import lombok.NoArgsConstructor;
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

    public ParamResolver paramResolver() {
        return new ParamResolver();
    }

    public BodyResolver bodyResolver() {
        return new BodyResolver();
    }

    public PathVariableResolver pathVariableResolver() {
        return new PathVariableResolver();
    }

    public HttpRequestResolver httpRequestResolver() {
        return new HttpRequestResolver();
    }

    public HttpResponseResolver httpResponseResolver() {
        return new HttpResponseResolver();
    }

    @Bean
    public ResolverMapping resolverMapping() {
        return new ResolverMapping(List.of(
                paramResolver(),
                bodyResolver(),
                pathVariableResolver(),
                httpRequestResolver(),
                httpResponseResolver()
        ));
    }

    @NoArgsConstructor
    public static class ResolverMapping {

        private Map<? extends Class<?>, Resolver> resolverMap;

        public ResolverMapping(final List<Resolver> resolvers) {
            resolverMap = Seq.of(resolvers).toMap(Resolver::getType);
        }

        public Resolver get(Class<?> annotationType) {
            return this.resolverMap.get(annotationType);
        }
    }
}