package io.github.fzdwx.resolver;

import io.github.fzdwx.inf.http.core.HttpServerRequest;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 16:44
 */
public interface Resolver {

    boolean support();

    Object resolve(HttpServerRequest request);
}