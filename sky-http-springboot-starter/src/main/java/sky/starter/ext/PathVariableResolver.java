package sky.starter.ext;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.lang.NvMap;
import sky.starter.domain.SkyHttpMethod;

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
    public Object apply(HttpServerRequest request, SkyHttpMethod.SkyHttpMethodParameter parameter, NvMap parVals) {
        final var parameterName = parameter.getParameterName();
        if (parameterName == null) {
            return null;
        }
        return parVals.get(parameterName);
    }
}