package core.http.ext;

import core.http.HttpServerRequest;
import core.http.HttpServerResponse;

/**
 * handle
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 12:26
 * @since 0.06
 */
@FunctionalInterface
public interface HttpHandler {

    /**
     * Process the given request, generating some data write to response.
     *
     * @param request  http request
     * @param response http response
     */
    void handle(HttpServerRequest request, final HttpServerResponse response);
}