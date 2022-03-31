package io.github.fzdwx.resolver;

import com.alibaba.fastjson.JSON;
import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/31 14:13
 */
public class BodyResolver implements Resolver {

    @Override
    public Object resolve(final HttpServerRequest request, final HttpServerResponse response, final Parameter parameter) {
        return JSON.parseObject(Netty.read(request.readJson()), parameter.getType());
    }

    @Override
    public Class<? extends Annotation> getType() {
        return RequestBody.class;
    }
}