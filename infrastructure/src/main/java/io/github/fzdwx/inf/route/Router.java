package io.github.fzdwx.inf.route;

import io.github.fzdwx.inf.HttpHandler;
import io.github.fzdwx.inf.http.core.HttpRequest;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.inf.route.inter.RouterTable;
import io.github.fzdwx.inf.route.inter.Routing;
import io.github.fzdwx.inf.route.inter.RoutingImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * router.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 11:40
 * @since 0.06
 */
public interface Router {

    String FAVICON_ICO = "/favicon.ico";

    /**
     * new router instance
     *
     * @since 0.06
     */
    static Router router() {
        return new RouterImpl();
    }

    /**
     * add route with path and handler.
     *
     * @since 0.06
     */
    default Router route(String path, RequestMethod methodType, HttpHandler httpHandler) {
        return route(path, methodType, 0, httpHandler);
    }

    /**
     * @since 0.06
     */
    default Router GET(String path, HttpHandler httpHandler) {
        return route(path, RequestMethod.GET, httpHandler);
    }

    /**
     * @since 0.06
     */
    default Router POST(String path, HttpHandler httpHandler) {
        return route(path, RequestMethod.POST, httpHandler);
    }

    /**
     * @since 0.06
     */
    default Router PUT(String path, HttpHandler httpHandler) {
        return route(path, RequestMethod.PUT, httpHandler);
    }

    /**
     * @since 0.06
     */
    default Router DELETE(String api, HttpHandler httpHandler) {
        return route(api, RequestMethod.DELETE, httpHandler);
    }

    /**
     * @since 0.06
     */
    default Router PATCH(String api, HttpHandler httpHandler) {
        return route(api, RequestMethod.PATCH, httpHandler);
    }

    /**
     * @since 0.06
     */
    default Router faviconIco(byte[] bytes) {
        return GET(FAVICON_ICO, (req, resp) -> resp.bytes(bytes));
    }

    /**
     * add route(base method)
     *
     * @since 0.06
     */
    Router route(String path, RequestMethod method, int index, HttpHandler httpHandler);

    /**
     * match one handler for request.
     *
     * @since 0.06
     */
    @Nullable
    HttpHandler matchOne(HttpRequest request);

    /**
     * get all request handlers.
     *
     * @since 0.06
     */
    RouterTable<HttpHandler> handlers();

    /**
     * clear handlers.
     *
     * @since 0.06
     */
    void clear();


    class RouterImpl implements Router {

        // for handler
        private final RouterTable<HttpHandler> httpHandlers;

        public RouterImpl() {
            this.httpHandlers = new RouterTable<>();
        }

        @Override
        public Router route(final String path, final RequestMethod method, final int index, final HttpHandler httpHandler) {
            Routing<HttpHandler> routing = new RoutingImpl<>(index, path, httpHandler, method);

            if (path.contains("*") || path.contains("{")) {
                httpHandlers.add(routing);
            } else {
                //没有*号的，优先
                httpHandlers.add(0, routing);
            }
            return this;
        }

        @Override
        public HttpHandler matchOne(HttpRequest request) {
            return httpHandlers.matchOne(request.uri(), request.methodType());
        }

        @Override
        public RouterTable<HttpHandler> handlers() {
            return httpHandlers;
        }

        @Override
        public void clear() {
            httpHandlers.clear();
        }

    }

}