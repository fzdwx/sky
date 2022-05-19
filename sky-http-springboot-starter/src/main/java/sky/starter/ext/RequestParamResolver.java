package sky.starter.ext;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.lang.NvMap;
import sky.starter.domain.SkyHttpMethod;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/19 20:27
 */
public class RequestParamResolver implements RequestArgumentResolver {

    @Override
    public boolean support(final SkyHttpMethod.SkyHttpMethodParameter parameter) {
        return parameter.getRequestParam() != null;
    }

    @Override
    public Object apply(final HttpServerRequest request, final SkyHttpMethod.SkyHttpMethodParameter parameter, final NvMap parVals) {
        return null;
    }
}