package sky.starter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;
import org.springframework.web.util.pattern.PathPatternParser;
import sky.starter.props.SkyHttpServerProps;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 17:33
 */
public class SkySpringbootHandlerMappingContainer extends HandlerMappingContainer<RequestMappingInfo> implements EmbeddedValueResolverAware {

    private Map<String, Predicate<Class<?>>> pathPrefixes = Collections.emptyMap();
    private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();
    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();
    private PathPatternParser patternParser = new PathPatternParser();
    private StringValueResolver embeddedValueResolver;

    public SkySpringbootHandlerMappingContainer(final ApplicationContext applicationContext, final SkyHttpServerProps skyHttpServerProps) {
        super(applicationContext, skyHttpServerProps);
        setNamingStrategy(new RequestMappingInfoHandlerMethodMappingNamingStrategy());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.config = new RequestMappingInfo.BuilderConfiguration();
        this.config.setTrailingSlashMatch(true);
        this.config.setContentNegotiationManager(contentNegotiationManager);

        this.config.setPatternParser(patternParser);

        super.afterPropertiesSet();
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(final Method method, final Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
            String prefix = getPathPrefix(handlerType);
            if (prefix != null) {
                info = RequestMappingInfo.paths(prefix).options(this.config).build().combine(info);
            }
        }
        return info;
    }

    @Override
    protected void registerHandlerMethod(final Object handler, final Method method, final RequestMappingInfo mapping) {
        super.registerHandlerMethod(handler, method, mapping);

        updateCondition(method, mapping);
    }

    @Override
    protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingInfo mappingInfo) {
        // HandlerMethod handlerMethod = createHandlerMethod(handler, method);
        // Class<?> beanType = handlerMethod.getBeanType();
        // CrossOrigin typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanType, CrossOrigin.class);
        // CrossOrigin methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, CrossOrigin.class);
        //
        // if (typeAnnotation == null && methodAnnotation == null) {
        //     return null;
        // }
        //
        // CorsConfiguration config = new CorsConfiguration();
        // updateCorsConfig(config, typeAnnotation);
        // updateCorsConfig(config, methodAnnotation);
        //
        // if (CollectionUtils.isEmpty(config.getAllowedMethods())) {
        //     for (RequestMethod allowedMethod : mappingInfo.getMethodsCondition().getMethods()) {
        //         config.addAllowedMethod(allowedMethod.name());
        //     }
        // }
        // return config.applyPermitDefaultValues();
        return null;
    }

    @Override
    protected Set<String> getDirectPaths(final RequestMappingInfo mapping) {
        return mapping.getDirectPaths();
    }

    @Override
    protected Set<String> getMappingPathPatterns(final RequestMappingInfo mapping) {
        return mapping.getPatternValues();
    }

    protected RequestMappingInfo createRequestMappingInfo(
            RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {

        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
                .methods(requestMapping.method())
                .params(requestMapping.params())
                .headers(requestMapping.headers())
                .consumes(requestMapping.consumes())
                .produces(requestMapping.produces())
                .mappingName(requestMapping.name());
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.options(this.config).build();
    }

    @Nullable
    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = (element instanceof Class ?
                getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
        return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
    }

    @Nullable
    String getPathPrefix(Class<?> handlerType) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : this.pathPrefixes.entrySet()) {
            if (entry.getValue().test(handlerType)) {
                String prefix = entry.getKey();
                if (this.embeddedValueResolver != null) {
                    prefix = this.embeddedValueResolver.resolveStringValue(prefix);
                }
                return prefix;
            }
        }
        return null;
    }

    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
        if (this.embeddedValueResolver == null) {
            return patterns;
        } else {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }

    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return null;
    }

    @Nullable
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return null;
    }

    private void updateCondition(final Method method, final RequestMappingInfo mapping) {
        ConsumesRequestCondition condition = mapping.getConsumesCondition();
        if (!condition.isEmpty()) {
            for (Parameter parameter : method.getParameters()) {
                MergedAnnotation<RequestBody> annot = MergedAnnotations.from(parameter).get(RequestBody.class);
                if (annot.isPresent()) {
                    condition.setBodyRequired(annot.getBoolean("required"));
                    break;
                }
            }
        }
    }
}