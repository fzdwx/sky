package sky.starter.bean;

import http.HttpServerRequest;
import http.HttpServerResponse;
import io.github.fzdwx.lambada.lang.NvMap;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.ext.RequestArgumentResolver;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 21:27
 */
public class PathVariableResolver implements RequestArgumentResolver {

    @Override
    public boolean support(final SkyHttpMethod.SkyHttpMethodParameter parameter) {
        return parameter.getPathVariable() != null;
    }

    @Override
    public Object apply(HttpServerRequest request, final HttpServerResponse response, SkyHttpMethod.SkyHttpMethodParameter parameter, NvMap parVals) {
        final String parameterName = parameter.getParameterName();
        if (parameterName == null) {
            return null;
        }
        return parVals.get(parameterName);
    }
}