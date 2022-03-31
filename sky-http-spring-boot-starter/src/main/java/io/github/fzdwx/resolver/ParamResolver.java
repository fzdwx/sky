package io.github.fzdwx.resolver;

import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 16:43
 */
public class ParamResolver implements Resolver {

    @Override
    public Object resolve(final HttpServerRequest request, final HttpServerResponse response, final Parameter parameter) {
        final var key = AnnotationUtils.getValue(AnnotationUtils.getAnnotation(parameter, RequestParam.class));
        return request.params().get(key);
    }

    @Override
    public Class<? extends Annotation> getType() {
        return RequestParam.class;
    }
}