package io.github.fzdwx.inf.route;

import io.github.fzdwx.inf.Handler;
import io.github.fzdwx.inf.http.HttpRequest;
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
 */
public interface Router {

    String FAVICON_ICO = "/favicon.ico";

    static Router router() {
        return new RouterImpl();
    }

    default Router route(String path, RequestMethod methodType, Handler handler) {
        return route(path, methodType, 0, handler);
    }

    default Router GET(String path, Handler handler) {
        return route(path, RequestMethod.GET, handler);
    }

    default Router POST(String path, Handler handler) {
        return route(path, RequestMethod.POST, handler);
    }

    default Router PUT(String path, Handler handler) {
        return route(path, RequestMethod.PUT, handler);
    }

    default Router DELETE(String api, Handler handler) {
        return route(api, RequestMethod.DELETE, handler);
    }

    default Router PATCH(String api, Handler handler) {
        return route(api, RequestMethod.PATCH, handler);
    }

    default Router faviconIco(byte[] bytes) {
        return GET(FAVICON_ICO, (req, resp) -> resp.bytes(bytes));
    }

    Router route(String path, RequestMethod method, int index, Handler handler);

    /**
     * 区配一个目标（根据上下文）
     */
    @Nullable
    Handler matchOne(HttpRequest request);

    RouterTable<Handler> handlers();

    /**
     * 清空路由关系
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