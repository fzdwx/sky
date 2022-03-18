package io.github.fzdwx.inf;

import io.github.fzdwx.inf.http.HttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * handle
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 12:26
 */
public interface Handler {

    void handle(FullHttpRequest request, final HttpResponse channel);
}