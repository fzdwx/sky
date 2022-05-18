package http;

import http.ext.HttpHandler;
import io.github.fzdwx.lambada.http.HttpMethod;

/**
 * router.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 11:40
 * @since 0.06
 */
public class Router {

    String FAVICON_ICO = "/favicon.ico";

    private final io.github.fzdwx.lambada.http.Router<HttpHandler> router;

    /**
     * new router instance
     *
     * @since 0.06
     */
    public static Router router() {
        return new Router();
    }

    public Router() {
        router = io.github.fzdwx.lambada.http.Router.router();
    }

    /**
     * @since 0.06
     */
    public Router faviconIco(byte[] bytes) {
        router.GET(FAVICON_ICO, (req, resp) -> resp.end(bytes));
        return this;
    }

    public Router GET(final String path, final HttpHandler handler) {
        router.GET(path, handler);
        return this;
    }

    public Router POST(final String path, final HttpHandler handler) {
        router.POST(path, handler);
        return this;
    }

    public Router add(final HttpMethod method, String path, final HttpHandler handler) {
        router.addRoute(method, path, handler);
        return this;
    }

    public io.github.fzdwx.lambada.http.Router.Route<HttpHandler> match(final HttpServerRequest req) {
        return router.match(req.methodType(), req.uri().split("\\?")[0]);
    }
}