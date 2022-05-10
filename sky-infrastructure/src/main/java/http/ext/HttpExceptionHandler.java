package http.ext;

import http.HttpServerRequest;
import http.HttpServerResponse;
import io.github.fzdwx.lambada.Coll;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/7 20:03
 */
@FunctionalInterface
public interface HttpExceptionHandler {

    void handler(HttpServerRequest req, HttpServerResponse resp, Throwable e);

    static HttpExceptionHandler defaultExceptionHandler(final HttpExceptionHandler exceptionHandler) {
        if (exceptionHandler != null) {
            return exceptionHandler;
        }

        return defaultExceptionHandler();
    }

    static HttpExceptionHandler defaultExceptionHandler() {
        return (req, resp, e) -> {
            resp.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            resp.json(
                    Coll.linkedMap(
                            "message", e.getMessage(),
                            "class", e.getClass(),
                            "cause", e.getCause(),
                            "stack", e.getStackTrace()
                    )
            );
        };
    }
}