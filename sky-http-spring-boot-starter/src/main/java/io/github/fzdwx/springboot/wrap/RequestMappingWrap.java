package io.github.fzdwx.springboot.wrap;

import cn.hutool.core.annotation.AnnotationUtil;
import io.github.fzdwx.inf.http.core.HttpHandler;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.resolver.Resolver;
import io.github.fzdwx.springboot.inject.ResolverInject;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/31 11:37
 */
public class RequestMappingWrap {

    private final Object source;
    private final Method method;
    private final boolean json;
    private final Map<Resolver, Parameter> resolvers;
    private RequestMethod requestMethod;
    private String path;

    private RequestMappingWrap(final Object source,
                               final Method method,
                               final boolean json,
                               final RequestMethod parentMethod,
                               final String parentPath,
                               final ResolverInject.ResolverMapping resolverMapping
    ) {
        this.source = source;
        this.method = method;
        this.json = json;
        this.initPathAndRequestMethod(parentPath, parentMethod);
        this.resolvers = this.initResolver(resolverMapping);
    }

    public static RequestMappingWrap create(final Object source, Method method,
                                            final boolean json,
                                            final String parentPath,
                                            final RequestMethod parentMethod,
                                            final ResolverInject.ResolverMapping resolverMapping
    ) {
        return new RequestMappingWrap(source, method, json, parentMethod, parentPath, resolverMapping);
    }

    public HttpHandler handler() {
        return (req, resp) -> {
            final Object res;
            if (!this.resolvers.isEmpty()) {
                res = method.invoke(source, parseArgs(req, resp));
            } else {
                res = method.invoke(source);
            }

            // TODO: 2022/3/31 如果是json，那么应该返回json
            if (!resp.isEnd()) {
                resp.end();
            }
        };
    }

    private Object[] parseArgs(final HttpServerRequest req, HttpServerResponse response) {
        final var objects = new Object[this.resolvers.size()];

        int i = 0;
        for (Map.Entry<Resolver, Parameter> entry : resolvers.entrySet()) {
            Resolver resolver = entry.getKey();
            Parameter parameter = entry.getValue();
            objects[i] = resolver.resolve(req, response, parameter);
            i++;
        }

        return objects;
    }

    public String path() {
        return this.path;
    }

    public RequestMethod requestMethod() {
        return this.requestMethod;
    }

    private Map<Resolver, Parameter> initResolver(final ResolverInject.ResolverMapping resolverMapping) {
        Map<Resolver, Parameter> map = new LinkedHashMap<>();
        Seq.of(this.method.getParameters())
                .forEach(p -> {
                    final var annotations = p.getAnnotations();

                    if (annotations == null || annotations.length == 0) {
                        Resolver resolver = resolverMapping.get(p.getType());
                        if (resolver != null) {
                            map.put(resolver, p);
                            return;
                        }
                    } else {
                        for (final Annotation annotation : annotations) {
                            Resolver resolver = resolverMapping.get(annotation.annotationType());
                            if (resolver != null) {
                                map.put(resolver, p);
                                return;
                            }
                        }
                    }
                    throw new UnsupportedOperationException("不支持的参数类型:" + p.getType());
                });
        return map;
    }

    private void initPathAndRequestMethod(final String parentPath, final RequestMethod parentMethod) {

        Seq.of(AnnotationUtil.getAnnotations(method, true))
                .forEach(annotation -> {

                    // init request method
                    if (annotation.annotationType().equals(RequestMapping.class)) {
                        final var a1 = (RequestMapping) annotation;
                        this.requestMethod = parentMethod == null ? RequestMethod.valueOf(a1.method()[0].name().toUpperCase(Locale.ROOT)) : parentMethod;
                    }

                    // init path
                    if (annotation.annotationType().equals(GetMapping.class)
                            || annotation.annotationType().equals(PostMapping.class)
                            || annotation.annotationType().equals(DeleteMapping.class)
                            || annotation.annotationType().equals(PutMapping.class)
                            || annotation.annotationType().equals(PatchMapping.class)) {
                        final var value = AnnotationUtils.getValue(annotation);
                        if (value == null) {
                            this.path = parentPath;
                        } else {
                            final var rawPath = ((String[]) value)[0];

                            if (parentPath.endsWith("/")) {
                                if (rawPath.startsWith("/")) {
                                    this.path = parentPath + rawPath.substring(1);
                                }
                            } else {
                                if (!rawPath.startsWith("/")) {
                                    this.path = parentPath + "/" + rawPath;
                                } else {
                                    this.path = parentPath + rawPath;
                                }
                            }
                        }
                    }
                });
    }
}