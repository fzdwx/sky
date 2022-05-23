package sky.starter.bean;

import http.HttpServerRequest;
import http.HttpServerResponse;
import http.ext.HttpHandler;
import io.github.fzdwx.lambada.http.HttpPath;
import io.github.fzdwx.lambada.http.Router;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sky.starter.domain.SkyHttpMethod;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.RequestArgumentResolver;
import sky.starter.ext.RequestResultHandler;

import java.util.Collection;

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
    private final Collection<RequestResultHandler> resultHandlers;
    private final Collection<RequestArgumentResolver> argumentResolvers;

    public DispatchHandler(final Router<SkyRouteDefinition> router,
                           final RequestResultHandlerContainer resultHandlers,
                           final RequestArgumentResolverContainer argumentResolvers) {
        this.router = router;
        this.resultHandlers = resultHandlers.container();
        this.argumentResolvers = argumentResolvers.container();
    }

    @SneakyThrows
    @Override
    public void handle(final HttpServerRequest request, final HttpServerResponse response) {
        final String path = HttpPath.format(request.path());
        final Router.Route<SkyRouteDefinition> route = router.match(request.methodType(), path);
        if (route == null) {
            notFound(request, response);
            return;
        }

        //TODO
        // 1. filter
        // 2. interceptor

        prepareHandle(route);

        final SkyRouteDefinition definition = route.handler();

        // step1. resolveArguments
        final Object[] arguments = resolveArguments(route, request, response);

        // step2. invoke target method
        final Object result = definition.invoke(arguments);

        // step3. parse result back to client
        final boolean handled = handlerResult(result, definition,request, response);

        if (!handled) {
            log.error("not found result handler for {}", definition.method());

            notSupport(request, response);
        }
    }

    private void prepareHandle(final Router.Route<SkyRouteDefinition> route) {
        // check bean is string ?
        createWithResolvedBean(route);
    }

    public boolean handlerResult(final Object result, final SkyRouteDefinition definition, final HttpServerRequest request, final HttpServerResponse response) {
        for (RequestResultHandler rh : resultHandlers) {
            if (rh.support(result, definition)) {
                rh.handle(result, definition,request, response);
                return true;
            }
        }
        return false;
    }

    private Object[] resolveArguments(final Router.Route<SkyRouteDefinition> route,
                                      final HttpServerRequest request,
                                      final HttpServerResponse response) {
        final SkyRouteDefinition definition = route.handler();

        final SkyHttpMethod.SkyHttpMethodParameter[] methodParameters = definition.getMethodParameters();
        if (methodParameters.length == 0) {
            return EMPTY_ARGS;
        }

        final Object[] arguments = new Object[methodParameters.length];
        final NvMap pathVal = route.extract(request.uri());

        for (int i = 0; i < methodParameters.length; i++) {
            final SkyHttpMethod.SkyHttpMethodParameter parameter = methodParameters[i];
            for (final RequestArgumentResolver paramResolver : this.argumentResolvers) {
                if (paramResolver.support(parameter)) {
                    arguments[i] = paramResolver.apply(request,response, parameter, pathVal);
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