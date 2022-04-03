package io.github.fzdwx.springboot.inject;

import cn.hutool.core.annotation.AnnotationUtil;
import io.github.fzdwx.RequestMounter;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.Tuple;
import io.github.fzdwx.lambada.internal.Tuple2;
import io.github.fzdwx.springboot.wrap.RequestMappingWrap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 14:36
 */
@RequiredArgsConstructor
public class RequestHandleInject implements BeanFactoryPostProcessor, ApplicationContextAware {

    private final Router router;
    private final ResolverInject.ResolverMapping resolverMapping;
    private final ParameterNameDiscoverer parameterNameDiscoverer;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (final RequestMounter bean : beanFactory.getBeansOfType(RequestMounter.class).values()) {
            bean.mount(router);
        }

        final Map<String, Object> controllerMap = beanFactory.getBeansWithAnnotation(Controller.class);

        controllerMap.forEach((k, v) -> {
            final RequestMapping requestMapping = AnnotationUtil.getAnnotation(v.getClass(), RequestMapping.class);
            final boolean jsonResponse = AnnotationUtil.getAnnotation(v.getClass(), ResponseBody.class) != null;

            // init parent path and method
            final String parentPath;
            final RequestMethod parentMethod;
            if (requestMapping == null) {
                parentPath = "/";
                parentMethod = null;
            } else {
                final Tuple2<String, RequestMethod> t2 = initParentPathAndMethod(requestMapping);
                parentPath = t2.v1;
                parentMethod = t2.v2;
            }

            // attach request handle to router
            Seq.of(ReflectionUtils.getDeclaredMethods(v.getClass()))
                    .filter(m -> m.isAnnotationPresent(RequestMapping.class)
                            || m.isAnnotationPresent(GetMapping.class)
                            || m.isAnnotationPresent(PostMapping.class)
                            || m.isAnnotationPresent(DeleteMapping.class)
                            || m.isAnnotationPresent(PutMapping.class)
                            || m.isAnnotationPresent(PatchMapping.class)
                    )
                    .map(m ->
                            RequestMappingWrap.create(
                                    v, m, jsonResponse, parentPath, parentMethod,
                                    resolverMapping,parameterNameDiscoverer
                            )
                    )
                    .forEach(w -> {
                        router.route(w.path(), w.requestMethod(), w.handler());
                    });

        });

    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    }

    private Tuple2<String, RequestMethod> initParentPathAndMethod(final RequestMapping requestMapping) {
        String parentPath = "";
        RequestMethod parentMethod = null;
        final var annotationAttributes = AnnotationUtils.getAnnotationAttributes(requestMapping);

        final var path = annotationAttributes.getOrDefault("value", annotationAttributes.getOrDefault("path", null));
        if (path == null) {
            throw new IllegalArgumentException("RequestMapping must have path or value");
        } else {
            parentPath = ((String[]) path)[0];
        }

        final var methods = ((org.springframework.web.bind.annotation.RequestMethod[]) annotationAttributes.getOrDefault("method", null));
        if (methods.length > 0) {
            parentMethod = RequestMethod.valueOf(methods[0].name().toUpperCase(Locale.ROOT));
        }

        return Tuple.of(parentPath, parentMethod);
    }
}