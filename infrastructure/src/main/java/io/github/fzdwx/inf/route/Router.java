package io.github.fzdwx.inf.route;

import io.github.fzdwx.inf.Handler;
import io.github.fzdwx.inf.Listener;
import io.github.fzdwx.inf.msg.ListenerWrapper;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.inf.route.inter.RouterTable;
import io.github.fzdwx.inf.route.inter.Routing;
import io.github.fzdwx.inf.route.inter.RoutingImpl;
import io.github.fzdwx.inf.route.msg.Session;
import io.netty.handler.codec.http.FullHttpRequest;

import static io.github.fzdwx.lambada.Lang.todo;

/**
 * router.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 11:40
 */
public interface Router {

    static Router router() {
        return new RouterImpl();
    }

    String FAVICON_ICO = "/favicon.ico";

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
    Handler matchOne(FullHttpRequest ctx, final RequestMethod requestMethod);

    default Router route(String path, Listener listener) {
        return route(path, RequestMethod.ALL, listener);
    }

    default Router route(String path, RequestMethod method, Listener listener) {
        return route(path, method, 0, listener);
    }

    Router route(String path, RequestMethod method, int index, Listener listener);

    /**
     * 区配一个目标（根据上上文）
     */
    Listener matchOne(Session session);

    /**
     * 清空路由关系
     */
    void clear();

    class RouterImpl implements Router {

        // for handler
        private final RouterTable<Handler> handlers;
        // for listener
        private final RouterTable<Listener> listeners;

        public RouterImpl() {
            this.handlers = new RouterTable<>();
            this.listeners = new RouterTable<>();
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
        public Handler matchOne(final FullHttpRequest ctx, final RequestMethod requestMethod) {
            return handlers.matchOne(ctx.uri(), requestMethod);
        }

        @Override
        public Router route(final String path, final RequestMethod method, final int index, final Listener listener) {
            Listener ln = new ListenerWrapper(path, listener);
            listeners.add(new RoutingImpl<>(index, path, ln, method));
            return this;
        }

        @Override
        public Listener matchOne(final Session session) {
            // TODO: 2022/3/18 socket Listener
            return todo();
        }

        @Override
        public void clear() {
            handlers.clear();
            listeners.clear();
        }
    }

}