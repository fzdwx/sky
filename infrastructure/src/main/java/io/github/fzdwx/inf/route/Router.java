package io.github.fzdwx.inf.route;

import io.github.fzdwx.inf.Handler;
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
    default Router route(String path, RequestMethod methodType, Handler handler) {
        return route(path, methodType, 0, handler);
    }

    /**
     * @since 0.06
     */
    default Router GET(String path, Handler handler) {
        return route(path, RequestMethod.GET, handler);
    }

    /**
     * @since 0.06
     */
    default Router POST(String path, Handler handler) {
        return route(path, RequestMethod.POST, handler);
    }

    /**
     * @since 0.06
     */
    default Router PUT(String path, Handler handler) {
        return route(path, RequestMethod.PUT, handler);
    }

    /**
     * @since 0.06
     */
    default Router DELETE(String api, Handler handler) {
        return route(api, RequestMethod.DELETE, handler);
    }

    /**
     * @since 0.06
     */
    default Router PATCH(String api, Handler handler) {
        return route(api, RequestMethod.PATCH, handler);
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
    Router route(String path, RequestMethod method, int index, Handler handler);

    /**
     * match one handler for request.
     *
     * @since 0.06
     */
    @Nullable
    Handler matchOne(HttpRequest request);

    /**
     * get all request handlers.
     *
     * @since 0.06
     */
    RouterTable<Handler> handlers();

    /**
     * clear handlers.
     *
     * @since 0.06
     */
    void clear();


    class RouterImpl implements Router {

        // for handler
        private final RouterTable<Handler> handlers;

        public RouterImpl() {
            this.handlers = new RouterTable<>();
        }

        @Override
        public Router route(final String path, final RequestMethod method, final int index, final Handler handler) {
            Routing<Handler> routing = new RoutingImpl<>(index, path, handler, method);

            if (path.contains("*") || path.contains("{")) {
                handlers.add(routing);
            } else {
                //没有*号的，优先
                handlers.add(0, routing);
            }
            return this;
        }

        @Override
        public Handler matchOne(HttpRequest request) {
            return handlers.matchOne(request.uri(), request.methodType());
        }

        @Override
        public RouterTable<Handler> handlers() {
            return handlers;
        }

        @Override
        public void clear() {
            handlers.clear();
        }

    }

}