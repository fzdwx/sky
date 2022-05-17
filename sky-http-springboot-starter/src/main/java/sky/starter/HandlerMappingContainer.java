package sky.starter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy;
import sky.starter.props.SkyHttpServerProps;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
public abstract class HandlerMappingContainer<T> implements InitializingBean {

    protected final ApplicationContext context;
    protected final SkyHttpServerProps skyHttpServerProps;
    private PathMatcher pathMatcher = new AntPathMatcher();

    @Nullable
    @Getter
    @Setter
    private HandlerMethodMappingNamingStrategy<T> namingStrategy;
    private final MappingRegistry mappingRegistry = new MappingRegistry();

    public HandlerMappingContainer(final ApplicationContext context, final SkyHttpServerProps skyHttpServerProps) {
        this.context = context;
        this.skyHttpServerProps = skyHttpServerProps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initHandler();
    }

    protected abstract T getMappingForMethod(final Method method, final Class<?> userType);

    /**
     * Create the HandlerMethod instance.
     *
     * @param handler either a bean name or an actual handler instance
     * @param method  the target method
     * @return the created HandlerMethod
     */
    protected HandlerMethod createHandlerMethod(Object handler, Method method) {
        if (handler instanceof String) {
            return new HandlerMethod((String) handler,
                    context.getAutowireCapableBeanFactory(),
                    context,
                    method);
        }
        return new HandlerMethod(handler, method);
    }

    protected void registerHandlerMethod(final Object handler, final Method method, final T mapping) {
        mappingRegistry.register(mapping, handler, method);
    }

    /**
     * Extract and return the CORS configuration for the mapping.
     */
    @Nullable
    protected CorsConfiguration initCorsConfiguration(Object handler, Method method, T mapping) {
        return null;
    }

    protected Set<String> getDirectPaths(T mapping) {
        Set<String> urls = Collections.emptySet();
        for (String path : getMappingPathPatterns(mapping)) {
            if (!pathMatcher.isPattern(path)) {
                urls = (urls.isEmpty() ? new HashSet<>(1) : urls);
                urls.add(path);
            }
        }
        return urls;
    }

    protected Set<String> getMappingPathPatterns(final T mapping) {
        return Collections.emptySet();
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
        Class<?> handlerType = (handler instanceof String ?
                context.getType((String) handler) : handler.getClass());
        if (handlerType == null) return;

        Class<?> userType = ClassUtils.getUserClass(handlerType);
        if (skyHttpServerProps.sky.debug) {
            log.info(DEBUG_PREFIX + "find handler {}", userType);
        }

        Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<T>) method -> {
                    try {
                        return getMappingForMethod(method, userType);
                    } catch (Throwable ex) {
                        throw new IllegalStateException("Invalid mapping on handler class [" +
                                userType.getName() + "]: " + method, ex);
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
        String formattedType = (StringUtils.hasText(packageName) ?
                Arrays.stream(packageName.split("\\."))
                        .map(packageSegment -> packageSegment.substring(0, 1))
                        .collect(Collectors.joining(".", "", "." + userType.getSimpleName())) :
                userType.getSimpleName());
        Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(",", "(", ")"));
        return methods.entrySet().stream()
                .map(e -> {
                    Method method = e.getKey();
                    return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
                })
                .collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
    }

    class MappingRegistry {

        private final Map<T, MappingRegistration<T>> registry = new HashMap<>();

        private final MultiValueMap<String, T> pathLookup = new LinkedMultiValueMap<>();

        private final Map<String, List<HandlerMethod>> nameLookup = new ConcurrentHashMap<>();

        private final Map<HandlerMethod, CorsConfiguration> corsLookup = new ConcurrentHashMap<>();

        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        /**
         * Return all registrations.
         *
         * @since 5.3
         */
        public Map<T, MappingRegistration<T>> getRegistrations() {
            return this.registry;
        }

        /**
         * Return matches for the given URL path. Not thread-safe.
         *
         * @see #acquireReadLock()
         */
        @Nullable
        public List<T> getMappingsByDirectPath(String urlPath) {
            return this.pathLookup.get(urlPath);
        }

        /**
         * Return handler methods by mapping name. Thread-safe for concurrent use.
         */
        public List<HandlerMethod> getHandlerMethodsByMappingName(String mappingName) {
            return this.nameLookup.get(mappingName);
        }

        /**
         * Return CORS configuration. Thread-safe for concurrent use.
         */
        @Nullable
        public CorsConfiguration getCorsConfiguration(HandlerMethod handlerMethod) {
            HandlerMethod original = handlerMethod.getResolvedFromHandlerMethod();
            return this.corsLookup.get(original != null ? original : handlerMethod);
        }

        /**
         * Acquire the read lock when using getMappings and getMappingsByUrl.
         */
        public void acquireReadLock() {
            this.readWriteLock.readLock().lock();
        }

        /**
         * Release the read lock after using getMappings and getMappingsByUrl.
         * todo
         * public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception
         */
        public void releaseReadLock() {
            this.readWriteLock.readLock().unlock();
        }

        public void register(T mapping, Object handler, Method method) {
            this.readWriteLock.writeLock().lock();
            try {
                HandlerMethod handlerMethod = createHandlerMethod(handler, method);
                validateMethodMapping(handlerMethod, mapping);

                Set<String> directPaths = getDirectPaths(mapping);
                for (String path : directPaths) {
                    this.pathLookup.add(path, mapping);
                }

                String name = null;
                if (getNamingStrategy() != null) {
                    name = getNamingStrategy().getName(handlerMethod, mapping);
                    addMappingName(name, handlerMethod);
                }

                // todo 跨域
                CorsConfiguration corsConfig = initCorsConfiguration(handler, method, mapping);
                if (corsConfig != null) {
                    corsConfig.validateAllowCredentials();
                    this.corsLookup.put(handlerMethod, corsConfig);
                }

                this.registry.put(mapping,
                        new MappingRegistration<>(mapping, handlerMethod, directPaths, name, corsConfig != null));
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }

        private void validateMethodMapping(HandlerMethod handlerMethod, T mapping) {
            MappingRegistration<T> registration = this.registry.get(mapping);
            HandlerMethod existingHandlerMethod = (registration != null ? registration.getHandlerMethod() : null);
            if (existingHandlerMethod != null && !existingHandlerMethod.equals(handlerMethod)) {
                throw new IllegalStateException(
                        "Ambiguous mapping. Cannot map '" + handlerMethod.getBean() + "' method \n" +
                                handlerMethod + "\nto " + mapping + ": There is already '" +
                                existingHandlerMethod.getBean() + "' bean method\n" + existingHandlerMethod + " mapped.");
            }
        }

        private void addMappingName(String name, HandlerMethod handlerMethod) {
            List<HandlerMethod> oldList = this.nameLookup.get(name);
            if (oldList == null) {
                oldList = Collections.emptyList();
            }

            for (HandlerMethod current : oldList) {
                if (handlerMethod.equals(current)) {
                    return;
                }
            }

            List<HandlerMethod> newList = new ArrayList<>(oldList.size() + 1);
            newList.addAll(oldList);
            newList.add(handlerMethod);
            this.nameLookup.put(name, newList);
        }

        public void unregister(T mapping) {
            this.readWriteLock.writeLock().lock();
            try {
                MappingRegistration<T> registration = this.registry.remove(mapping);
                if (registration == null) {
                    return;
                }

                for (String path : registration.getDirectPaths()) {
                    List<T> mappings = this.pathLookup.get(path);
                    if (mappings != null) {
                        mappings.remove(registration.getMapping());
                        if (mappings.isEmpty()) {
                            this.pathLookup.remove(path);
                        }
                    }
                }

                removeMappingName(registration);

                this.corsLookup.remove(registration.getHandlerMethod());
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }

        private void removeMappingName(MappingRegistration<T> definition) {
            String name = definition.getMappingName();
            if (name == null) {
                return;
            }
            HandlerMethod handlerMethod = definition.getHandlerMethod();
            List<HandlerMethod> oldList = this.nameLookup.get(name);
            if (oldList == null) {
                return;
            }
            if (oldList.size() <= 1) {
                this.nameLookup.remove(name);
                return;
            }
            List<HandlerMethod> newList = new ArrayList<>(oldList.size() - 1);
            for (HandlerMethod current : oldList) {
                if (!current.equals(handlerMethod)) {
                    newList.add(current);
                }
            }
            this.nameLookup.put(name, newList);
        }
    }


    static class MappingRegistration<T> {

        private final T mapping;

        private final HandlerMethod handlerMethod;

        private final Set<String> directPaths;

        @Nullable
        private final String mappingName;

        private final boolean corsConfig;

        public MappingRegistration(T mapping, HandlerMethod handlerMethod,
                                   @Nullable Set<String> directPaths, @Nullable String mappingName, boolean corsConfig) {

            Assert.notNull(mapping, "Mapping must not be null");
            Assert.notNull(handlerMethod, "HandlerMethod must not be null");
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
            this.directPaths = (directPaths != null ? directPaths : Collections.emptySet());
            this.mappingName = mappingName;
            this.corsConfig = corsConfig;
        }

        public T getMapping() {
            return this.mapping;
        }

        public HandlerMethod getHandlerMethod() {
            return this.handlerMethod;
        }

        public Set<String> getDirectPaths() {
            return this.directPaths;
        }

        @Nullable
        public String getMappingName() {
            return this.mappingName;
        }

        public boolean hasCorsConfig() {
            return this.corsConfig;
        }
    }


    /**
     * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
     * comparing the best match with a comparator in the context of the current request.
     */
    private class Match {

        private final T mapping;

        private final MappingRegistration<T> registration;

        public Match(T mapping, MappingRegistration<T> registration) {
            this.mapping = mapping;
            this.registration = registration;
        }

        public HandlerMethod getHandlerMethod() {
            return this.registration.getHandlerMethod();
        }

        public boolean hasCorsConfig() {
            return this.registration.hasCorsConfig();
        }

        @Override
        public String toString() {
            return this.mapping.toString();
        }
    }


    private class MatchComparator implements Comparator<Match> {

        private final Comparator<T> comparator;

        public MatchComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Match match1, Match match2) {
            return this.comparator.compare(match1.mapping, match2.mapping);
        }
    }


    private static class EmptyHandler {

        @SuppressWarnings("unused")
        public void handle() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

}