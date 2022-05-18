package sky.starter.ext;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.lang.NvMap;
import org.springframework.core.MethodParameter;

/**
 * request param resolver.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 17:03
 */
public interface RequestParamResolver {

    boolean support(MethodParameter parameter);

    Object apply(HttpServerRequest request, MethodParameter parameter, NvMap parVals);
}