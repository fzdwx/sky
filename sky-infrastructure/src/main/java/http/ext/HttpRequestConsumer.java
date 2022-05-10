package http.ext;

import http.HttpServerRequest;
import http.HttpServerResponse;


/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 16:39
 */
@FunctionalInterface
public interface HttpRequestConsumer {

    void consume(final HttpServerRequest req, final HttpServerResponse response);
}