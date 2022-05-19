package sky.starter;

import http.HttpServerRequest;
import http.HttpServerResponse;
import http.ext.HttpHandler;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.http.HttpPath;
import io.github.fzdwx.lambada.http.Router;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.RequestArgumentResolver;
import sky.starter.ext.RequestResultHandler;

import java.util.List;

/**
 * dispatch handler.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 15:00
 */
@Slf4j
public class DispatchHandler implements HttpHandler {

    static Object[] EMPTY_ARGS = new Object[0];
    private final Router<SkyRouteDefinition> router;
    private final List<RequestResultHandler> resultHandlers;
    private final List<RequestArgumentResolver> argumentResolvers;

    public DispatchHandler(final Router<SkyRouteDefinition> router,
                           final List<RequestResultHandler> resultHandlers,
                           final List<RequestArgumentResolver> argumentResolvers) {
        this.router = router;
        this.resultHandlers = Seq.sort(resultHandlers, RequestResultHandler.sort);
        this.argumentResolvers = argumentResolvers;
    }

    @SneakyThrows
    @Override
    public void handle(final HttpServerRequest request, final HttpServerResponse response) {
        final String path = HttpPath.format(request.uri());
        final Router.Route<SkyRouteDefinition> route = router.match(request.methodType(), path);
        if (route == null) {
            notFound(request, response);
            return;
        }

        // check bean is string ?
        createWithResolvedBean(route);

        final SkyRouteDefinition definition = route.handler();

        final var arguments = resolveArguments(route, request);
        if (!handlerResult(definition.invoke(arguments), definition, response)) {
            log.error("not found result handler for {}", definition.method());

            // todo 要不要一个默认的处理器？
            notSupport(request, response);
        }
    }

    public boolean handlerResult(final Object result, final SkyRouteDefinition definition, final HttpServerResponse response) {
        for (RequestResultHandler rh : resultHandlers) {
            if (rh.support(result, definition)) {
                rh.apply(result, definition, response);
                return true;
            }
        }
        return false;
    }

    private Object[] resolveArguments(final Router.Route<SkyRouteDefinition> route, final HttpServerRequest request) {
        final var definition = route.handler();

        final var methodParameters = definition.getMethodParameters();
        if (methodParameters.length == 0) {
            return EMPTY_ARGS;
        }

        final var arguments = new Object[methodParameters.length];
        final var pathVal = route.extract(request.uri());

        for (int i = 0; i < methodParameters.length; i++) {
            final SkyHttpMethod.SkyHttpMethodParameter parameter = methodParameters[i];
            for (final RequestArgumentResolver paramResolver : this.argumentResolvers) {
                if (paramResolver.support(parameter)) {
                    arguments[i] = paramResolver.apply(request, parameter, pathVal);
                    break;
                }
            }
        }

        return arguments;
    }

    private void createWithResolvedBean(final Router.Route<SkyRouteDefinition> route) {
        final SkyRouteDefinition definition = route.handler();
        if (route.handler().method().getBean() instanceof String) {
            definition.createWithResolvedBean();
        }
    }

    private void notFound(final HttpServerRequest request, final HttpServerResponse response) {
        response.notFound(String.format("%s '%s' not found", request.methodType(), request.uri()));
    }

    private void notSupport(final HttpServerRequest request, final HttpServerResponse response) {
        response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR).end(String.format("%s '%s' not found result handler", request.methodType(), request.uri()));
    }
}