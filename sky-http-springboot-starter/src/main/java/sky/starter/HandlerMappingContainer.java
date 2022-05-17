package sky.starter;

import http.HttpServerRequest;
import http.ext.HttpHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy;
import org.springframework.web.util.pattern.PathPatternParser;
import sky.starter.ext.MappingRegistry;
import sky.starter.props.SkyHttpServerProps;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static sky.starter.Utils.DEBUG_PREFIX;

/**
 * http handler mapping container
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 17:10
 */
@Slf4j
public abstract class HandlerMappingContainer<T> implements InitializingBean, EmbeddedValueResolverAware {

    protected final ApplicationContext context;
    protected final SkyHttpServerProps skyHttpServerProps;
    private final MappingRegistry<T> mappingRegistry = new MappingRegistry<>();
    protected StringValueResolver stringValueResolver;
    protected PathPatternParser patternParser = new PathPatternParser();
    @Nullable
    @Getter
    @Setter
    private HandlerMethodMappingNamingStrategy<T> namingStrategy;

    public HandlerMappingContainer(final ApplicationContext context, final SkyHttpServerProps skyHttpServerProps) {
        this.context = context;
        this.skyHttpServerProps = skyHttpServerProps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initHandler();
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }

    /**
     * Create the HandlerMethod instance.
     *
     * @param handler either a bean name or an actual handler instance
     * @param method  the target method
     * @return the created HandlerMethod
     */
    public HandlerMethod createHandlerMethod(Object handler, Method method) {
        if (handler instanceof String) {
            return new HandlerMethod((String) handler, context.getAutowireCapableBeanFactory(), context, method);
        }
        return new HandlerMethod(handler, method);
    }

    protected abstract T getMappingForMethod(final Method method, final Class<?> userType);

    protected HttpHandler getHandler(HttpServerRequest request) {
        // HandlerMethod handlerMethod = lookupHandlerMethod(request.uri(), request);
        return null;
    }

    /**
     * register handler
     *
     * @param handler maybe is class or entity.
     */
    protected void registerHandlerMethod(final Object handler, final Method method, final T mapping) {
        mappingRegistry.register(mapping, handler, method);
    }

    private void initHandler() {
        final String[] beanNames = context.getBeanNamesForType(Object.class);
        for (final String beanName : beanNames) {
            Class<?> beanType = null;
            try {
                beanType = context.getType(beanName);
            } catch (Exception e) {
                if (skyHttpServerProps.sky.debug) {
                    log.info(DEBUG_PREFIX + "could not resolve type for bean {}", beanName, e);
                }
            }

            if (beanType != null && HandlerChecker.isHandler(beanType)) {
                collectHandler(beanType);
            }
        }
    }

    /**
     * @param handler handler instance or handler class.
     */
    private void collectHandler(final Object handler) {
        if (handler == null) return;
        Class<?> handlerType;
        if (!(handler instanceof Class<?> ht)) {
            handlerType = (handler instanceof String ? context.getType((String) handler) : handler.getClass());
            if (handlerType == null) return;
        } else {
            handlerType = ht;
        }

        Class<?> userType = ClassUtils.getUserClass(handlerType);
        if (skyHttpServerProps.sky.debug) {
            log.info(DEBUG_PREFIX + "find handler {}", userType);
        }

        Map<Method, T> methods = MethodIntrospector.selectMethods(userType, (MethodIntrospector.MetadataLookup<T>) method -> {
            try {
                return getMappingForMethod(method, userType);
            } catch (Throwable ex) {
                throw new IllegalStateException("Invalid mapping on handler class [" + userType.getName() + "]: " + method, ex);
            }
        });

        if (skyHttpServerProps.sky.debug) {
            log.info(DEBUG_PREFIX + formatMappings(userType, methods));
        }

        methods.forEach((method, mapping) -> {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
            registerHandlerMethod(handler, invocableMethod, mapping);
        });
    }

    private String formatMappings(Class<?> userType, Map<Method, T> methods) {
        String packageName = ClassUtils.getPackageName(userType);
        String formattedType = (StringUtils.hasText(packageName) ? Arrays.stream(packageName.split("\\.")).map(packageSegment -> packageSegment.substring(0, 1)).collect(Collectors.joining(".", "", "." + userType.getSimpleName())) : userType.getSimpleName());
        Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",", "(", ")"));
        return methods.entrySet().stream().map(e -> {
            Method method = e.getKey();
            return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
        }).collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
    }
}