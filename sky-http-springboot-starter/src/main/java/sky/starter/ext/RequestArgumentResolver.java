package sky.starter.ext;

import http.HttpServerRequest;
import io.github.fzdwx.lambada.lang.NvMap;
import sky.starter.bean.impl.PathVariableResolver;
import sky.starter.bean.impl.RequestParamResolver;
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

    Object apply(HttpServerRequest request, SkyHttpMethod.SkyHttpMethodParameter parameter, NvMap parVals);
}