package sky.starter.ext;

import http.HttpServerRequest;
import http.HttpServerResponse;
import io.github.fzdwx.lambada.lang.NvMap;
import sky.starter.bean.PathVariableResolver;
import sky.starter.bean.RequestParamResolver;
import sky.starter.domain.SkyHttpMethod;

/**
 * request param resolver.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 17:03
 * @see PathVariableResolver
 * @see RequestParamResolver
 */
public interface RequestArgumentResolver {

    boolean support(SkyHttpMethod.SkyHttpMethodParameter parameter);

    Object apply(HttpServerRequest request, final HttpServerResponse response, SkyHttpMethod.SkyHttpMethodParameter parameter, NvMap parVals);
}