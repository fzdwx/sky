package core.http.route;

import core.http.ext.HttpHandler;
import io.github.fzdwx.lambada.lang.NvMap;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/25 17:22
 */
public class Route implements io.github.fzdwx.lambada.http.Route<HttpHandler> {

    private final io.github.fzdwx.lambada.http.Route<HttpHandler> source;

    public Route(final io.github.fzdwx.lambada.http.Route<HttpHandler> source) {
        this.source = source;
    }

    @Override
    public String pattern() {
        return source.pattern();
    }

    @Override
    public HttpHandler handler() {
        return source.handler();
    }

    @Override
    public NvMap extract(final String path) {
        return source.extract(path);
    }

    public static Route from(final io.github.fzdwx.lambada.http.Route<HttpHandler> source) {
        return new Route(source);
    }
}