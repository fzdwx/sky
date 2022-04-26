package org.atomicode.fzdwx.springboot.inject;

import lombok.NoArgsConstructor;
import org.atomicode.fzdwx.lambada.Seq;
import org.atomicode.fzdwx.resolver.BodyResolver;
import org.atomicode.fzdwx.resolver.HttpRequestResolver;
import org.atomicode.fzdwx.resolver.HttpResponseResolver;
import org.atomicode.fzdwx.resolver.ParamResolver;
import org.atomicode.fzdwx.resolver.PathVariableResolver;
import org.atomicode.fzdwx.resolver.Resolver;
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