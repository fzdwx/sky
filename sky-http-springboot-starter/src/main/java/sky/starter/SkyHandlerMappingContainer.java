package sky.starter;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import sky.starter.props.SkyHttpServerProps;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 22:04
 */
public class SkyHandlerMappingContainer extends HandlerMappingContainer<SkyHandlerInfo> {

    public SkyHandlerMappingContainer(final ApplicationContext context,
                                      final SkyHttpServerProps skyHttpServerProps) {
        super(context, skyHttpServerProps);
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