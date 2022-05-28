package sky.starter.bean;

import core.http.HttpServerResponse;
import org.springframework.core.Ordered;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.RequestResultHandler;

/**
 * support every response result.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 17:43
 */
public class EveryRequestResultHandler implements RequestResultHandler {

    @Override
    public boolean support(final Object result, final SkyRouteDefinition definition) {
        return true;
    }

    @Override
    public void apply(final Object result, final SkyRouteDefinition definition, final HttpServerResponse response) {
        response.end(result.toString());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}