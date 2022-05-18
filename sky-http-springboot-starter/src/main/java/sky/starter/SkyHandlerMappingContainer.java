package sky.starter;

import http.HttpServerRequest;
import http.ext.HttpHandler;
import io.github.fzdwx.lambada.http.Router;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import sky.starter.domain.SkyHandlerInfo;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.HandlerMappingContainer;
import sky.starter.props.SkyHttpServerProps;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * sky http handler container.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 22:04
 */
public class SkyHandlerMappingContainer extends HandlerMappingContainer<SkyHandlerInfo> {

    final Router<SkyRouteDefinition> router;

    public SkyHandlerMappingContainer(final SkyHttpServerProps skyHttpServerProps, final Router<SkyRouteDefinition> router) {
        super(skyHttpServerProps);
        this.router = router;
    }

    @Override
    protected SkyHandlerInfo getMappingForMethod(final Method method, final Class<?> handlerType) {
        var info = createInfo(method);
        if (info != null) {
            final var typeInfo = createInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
        }
        return info;
    }

    @Override
    protected HttpHandler getHandler(final HttpServerRequest request) {
        return null;
    }

    @Override
    protected void registerHandlerMethod(final Object handler, final Method method, final SkyHandlerInfo skyHandlerInfo) {
        final HandlerMethod handlerMethod = createHandlerMethod(handler, method);
        skyHandlerInfo.addToRouter(router, handlerMethod);
    }

    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
        if (this.stringValueResolver == null) {
            return patterns;
        } else {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                resolvedPatterns[i] = this.stringValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }

    @Nullable
    private SkyHandlerInfo createInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        if (requestMapping == null) {
            return null;
        }
        return SkyHandlerInfo
                .paths(patternParser, resolveEmbeddedValuesInPatterns(requestMapping.path()))
                .methods(requestMapping.method())
                .headers(requestMapping.headers())
                .consumer(requestMapping.consumes())
                .producer(requestMapping.produces())
                .name(requestMapping.name());
    }

}