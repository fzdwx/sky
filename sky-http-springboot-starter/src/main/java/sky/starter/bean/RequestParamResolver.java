package sky.starter.bean;

import http.HttpServerRequest;
import http.HttpServerResponse;
import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.lang.NvMap;
import lombok.RequiredArgsConstructor;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.ext.RequestArgumentResolver;
import sky.starter.ext.ValConvertor;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/19 20:27
 */
@RequiredArgsConstructor
public class RequestParamResolver implements RequestArgumentResolver {

    private final ValConvertor valConvertor;

    @Override
    public boolean support(final SkyHttpMethod.SkyHttpMethodParameter parameter) {
        return parameter.getRequestParam() != null;
    }

    @Override
    public Object apply(final HttpServerRequest request, final HttpServerResponse response, final SkyHttpMethod.SkyHttpMethodParameter parameter,
                        final NvMap parVals) {

        final java.util.Map<String, Object> attr = parameter.getRequestParamAttr();
        final boolean required = (boolean) attr.get("required");
        final String name = (String) attr.get("name");
        final String paramName = Lang.isBlank(name) ? parameter.getParameterName() : name;
        final String res = request.params().get(paramName);

        checkRequired(required, paramName, res);

        final Object convert = valConvertor.convert(res, parameter.getParameterType());

        checkRequired(required, paramName, convert);

        return convert;
    }

    private void checkRequired(final boolean required, final String paramName, final Object obj) {
        if (required) {
            Assert.nonNull(obj, "required param [ " + paramName + " ] is null");
        }
    }
}