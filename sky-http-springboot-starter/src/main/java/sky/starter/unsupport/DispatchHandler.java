package sky.starter.unsupport;

import http.HttpServerRequest;
import http.HttpServerResponse;
import http.ext.HttpHandler;
import io.github.fzdwx.lambada.http.HttpPath;
import io.github.fzdwx.lambada.http.Router;
import sky.starter.domain.SkyRouteDefinition;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 15:00
 */
public class DispatchHandler implements HttpHandler {

    private final Router<SkyRouteDefinition> router;

    public DispatchHandler(final Router<SkyRouteDefinition> router) { this.router = router; }

    @Override
    public void handle(final HttpServerRequest request, final HttpServerResponse response) {
        final String path = HttpPath.format(request.uri());
        final Router.Route<SkyRouteDefinition> route = router.match(request.methodType(), path);
        response.notFound("404");
    }
}