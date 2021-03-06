package sky.starter.bean;

import core.http.ext.HttpServerRequest;
import core.http.ext.HttpServerResponse;
import io.github.fzdwx.lambada.lang.KvMap;
import org.springframework.web.bind.annotation.RequestBody;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.ext.RequestArgumentResolver;

/**
 * support {@link RequestBody}
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/21 17:31
 */
public class RequestBodyResolver implements RequestArgumentResolver {

    @Override
    public boolean support(final SkyHttpMethod.SkyHttpMethodParameter parameter) {
        return parameter.getRequestBody() != null;
    }

    @Override
    public Object apply(final HttpServerRequest request, final HttpServerResponse response, final SkyHttpMethod.SkyHttpMethodParameter parameter, final KvMap parVals) {
        return request.serializer().toBean(request.bodyToString(), parameter.getParameterType());
    }
}