package org.atomicode.fzdwx.resolver;

import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import org.atomicode.fzdwx.springboot.wrap.ParameterWrap;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/31 15:31
 */
public class HttpResponseResolver implements Resolver {

    @Override
    public Object resolve(final HttpServerRequest request, final HttpServerResponse response, final ParameterWrap parameter) {
        return response;
    }

    @Override
    public Class<?> getType() {
        return HttpServerResponse.class;
    }
}