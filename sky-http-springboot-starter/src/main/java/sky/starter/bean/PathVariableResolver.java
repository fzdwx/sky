package sky.starter.bean;

import core.http.ext.HttpServerRequest;
import core.http.ext.HttpServerResponse;
import io.github.fzdwx.lambada.lang.NvMap;
import org.springframework.web.bind.annotation.PathVariable;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.ext.RequestArgumentResolver;

/**
 * support {@link PathVariable}
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