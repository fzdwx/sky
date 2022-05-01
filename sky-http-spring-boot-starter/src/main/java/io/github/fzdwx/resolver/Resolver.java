package io.github.fzdwx.resolver;

import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import io.github.fzdwx.springboot.wrap.ParameterWrap;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/29 16:44
 */
public interface Resolver {

    Object resolve(HttpServerRequest request, final HttpServerResponse response, final ParameterWrap parameter);

    Class<?> getType();
}