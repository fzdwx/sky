package io.github.fzdwx.resolver;

import cn.hutool.core.convert.Convert;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import io.github.fzdwx.springboot.wrap.ParameterWrap;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * resolver for path variable
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/4/3 11:42
 * @see PathVariable
 */
public class PathVariableResolver implements Resolver {

    @Override
    public Object resolve(final HttpServerRequest request, final HttpServerResponse response, final ParameterWrap parameter) {
        return Convert.convert(parameter.getType(), request.pathVar().get(parameter.getArgName()));
    }

    @Override
    public Class<?> getType() {
        return PathVariable.class;
    }
}