package io.github.fzdwx.resolver;

import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;

import java.lang.reflect.Parameter;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/31 15:30
 */
public class HttpRequestResolver implements Resolver {

    @Override
    public Object resolve(final HttpServerRequest request, final HttpServerResponse response, final Parameter parameter) {
        return request;
    }

    @Override
    public Class<?> getType() {
        return HttpServerRequest.class;
    }
}