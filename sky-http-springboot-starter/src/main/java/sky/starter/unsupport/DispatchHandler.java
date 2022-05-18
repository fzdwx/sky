package sky.starter.unsupport;

import http.HttpServerRequest;
import http.HttpServerResponse;
import http.ext.HttpHandler;
import io.github.fzdwx.lambada.http.Router;
import org.springframework.web.method.HandlerMethod;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 15:00
 */
public class DispatchHandler implements HttpHandler {

    private final Router<HandlerMethod> router;

    public DispatchHandler(final Router<HandlerMethod> router) { this.router = router; }

    @Override
    public void handle(final HttpServerRequest request, final HttpServerResponse response) {
        response.notFound("404");
    }
}