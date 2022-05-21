package sky.starter.bean.impl;

import http.HttpServerRequest;
import http.HttpServerResponse;
import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.lang.NvMap;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.ext.RequestArgumentResolver;

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
    public Object apply(final HttpServerRequest request, final HttpServerResponse response, final SkyHttpMethod.SkyHttpMethodParameter parameter, final NvMap parVals) {
        String res = null;

        final var attr = parameter.getRequestParamAttr();
        final var name = attr.get("name");
        final var required = attr.get("required");

        if (name != null) {
            res = request.params().get(name);
        } else {
            res = request.params().get(parameter.getParameterName());
        }

        if (((boolean) required)) {
            Assert.nonNull(res, "required param is null");
        }

        // todo 类型参数转换
        return res;
    }
}