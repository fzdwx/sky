package sky.starter.bean;

import core.http.ext.HttpServerResponse;
import core.http.response.HttpResponse;
import org.springframework.core.Ordered;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.RequestResultHandler;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/6/12 14:45
 */
public class SkyResponseRequestResultHandler implements RequestResultHandler {

    @Override
    public boolean support(final Object result, final SkyRouteDefinition definition) {
        if (result == null) {
            return false;
        }
        return result instanceof HttpResponse<?>;
    }

    @Override
    public void apply(final Object result, final SkyRouteDefinition definition, final HttpServerResponse response) {
        final HttpResponse<?> httpResponse = (HttpResponse<?>) result;
        response.end(httpResponse);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 400;
    }
}