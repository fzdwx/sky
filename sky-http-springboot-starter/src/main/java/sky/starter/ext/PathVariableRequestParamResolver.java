package sky.starter.ext;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.lang.NvMap;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 21:27
 */
public class PathVariableRequestParamResolver implements RequestParamResolver {

    @Override
    public boolean support(final MethodParameter parameter) {
        return parameter.getAnnotatedElement().getAnnotation(PathVariable.class) != null;
    }

    @Override
    public Object apply(HttpServerRequest request, MethodParameter parameter, NvMap parVals) {
        final var parameterName = parameter.getParameterName();
        if (parameterName == null) {
            return null;
        }
        return parVals.get(parameterName);
    }
}