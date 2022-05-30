package sky.starter.ext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy;
import org.springframework.web.util.pattern.PathPatternParser;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.props.SkyWebServerProps;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static sky.starter.util.Utils.DEBUG_PREFIX;

/**
 * http handler mapping container
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 17:10
 */
@Slf4j
public abstract class HandlerMappingContainer<T> implements InitializingBean, EmbeddedValueResolverAware, ApplicationContextAware {

    protected final SkyWebServerProps skyWebServerProps;
    protected ApplicationContext context;
    protected StringValueResolver stringValueResolver;
    protected PathPatternParser patternParser = new PathPatternParser();
    @Nullable
    @Getter
    @Setter
    private HandlerMethodMappingNamingStrategy<T> namingStrategy;

    public HandlerMappingContainer(final SkyWebServerProps skyWebServerProps) {
        this.skyWebServerProps = skyWebServerProps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initHandler();
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * Create the HandlerMethod instance.
     *
     * @param handler either a bean name or an actual handler instance
     * @param method  the target method
     * @return the created HandlerMethod
     */
    public SkyHttpMethod createHandlerMethod(Object handler, Method method) {
        final SkyHttpMethod handlerMethod;
        if (handler instanceof String) {
            handlerMethod = new SkyHttpMethod((String) handler, context.getAutowireCapableBeanFactory(), context, method);
        } else {
            handlerMethod = new SkyHttpMethod(handler, method);
        }

        return handlerMethod;
    }

    protected abstract T getMappingForMethod(final Method method, final Class<?> userType);

    protected abstract void registerHandlerMethod(final Object handler, final Method method, final T mapping);

    private void initHandler() {
        final String[] beanNames = context.getBeanNamesForType(Object.class);
        for (final String beanName : beanNames) {
            Class<?> beanType = null;
            try {
                beanType = context.getType(beanName);
            } catch (Exception e) {
                if (skyWebServerProps.enableDebug()) {
                    log.info(DEBUG_PREFIX + "could not resolve type for bean {}", beanName, e);
                }
            }

            if (beanType != null && isHandler(beanType)) {
                collectHandler(beanName);
            }
        }
    }

    /**
     * @param handler handler instance or handler class.
     */
    private void collectHandler(final Object handler) {
        if (handler == null) return;
        Class<?> handlerType = (handler instanceof String ?
                context.getType((String) handler) : handler.getClass());
        if (handlerType == null) {
            return;
        }

        Class<?> userType = ClassUtils.getUserClass(handlerType);
        if (skyWebServerProps.enableDebug()) {
            log.info(DEBUG_PREFIX + "find handler {}", userType);
        }

        Map<Method, T> methods = MethodIntrospector.selectMethods(userType, (MethodIntrospector.MetadataLookup<T>) method -> {
            try {
                return getMappingForMethod(method, userType);
            } catch (Throwable ex) {
                throw new IllegalStateException("Invalid mapping on handler class [" + userType.getName() + "]: " + method, ex);
            }
        });

        if (skyWebServerProps.enableDebug()) {
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

    private boolean isHandler(Class<?> beanType) {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
                AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }
}