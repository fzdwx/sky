package sky.starter.bean;

import core.http.ext.HttpServerRequest;
import core.http.ext.HttpServerResponse;
import io.github.fzdwx.lambada.lang.KvMap;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.ext.RequestArgumentResolver;

/**
 * support {@link HttpServerResponse}.
 *
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
                        final KvMap parVals) {
        return response;
    }
}