package io.github.fzdwx.resolver;

import io.github.fzdwx.inf.http.core.HttpServerRequest;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 16:43
 */
public class ParamResolver implements Resolver {

    @Override
    public boolean support() {
        return false;
    }

    @Override
    public Object resolve(final HttpServerRequest request) {
        return null;
    }
}