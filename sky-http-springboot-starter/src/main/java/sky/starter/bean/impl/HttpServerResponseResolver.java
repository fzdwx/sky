package sky.starter.bean.impl;

import http.HttpServerRequest;
import http.HttpServerResponse;
import io.github.fzdwx.lambada.lang.NvMap;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.ext.RequestArgumentResolver;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/21 18:19
 */
public class HttpServerResponseResolver implements RequestArgumentResolver {

    @Override
    public boolean support(final SkyHttpMethod.SkyHttpMethodParameter parameter) {
        return HttpServerResponse.class.equals(parameter.getParameterType());
    }

    @Override
    public Object apply(final HttpServerRequest request, final HttpServerResponse response, final SkyHttpMethod.SkyHttpMethodParameter parameter,
                        final NvMap parVals) {
        return response;
    }
}