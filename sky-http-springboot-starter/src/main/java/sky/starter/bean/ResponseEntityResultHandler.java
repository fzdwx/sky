package sky.starter.bean;

import core.http.ext.HttpServerResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.RequestResultHandler;

/**
 * support {@link ResponseEntity}
 * <p>
 * 1. currently it simply treats the return value as json...
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/21 17:59
 */
public class ResponseEntityResultHandler implements RequestResultHandler {

    @Override
    public boolean support(final Object result, final SkyRouteDefinition definition) {
        return result instanceof ResponseEntity<?>;
    }

    @Override
    public void apply(final Object result, final SkyRouteDefinition definition, final HttpServerResponse response) {
        final ResponseEntity<?> entity = (ResponseEntity<?>) result;
        response.status(HttpResponseStatus.valueOf(entity.getStatusCode().value()));

        entity.getHeaders().forEach((k, v) -> {
            response.header(k, v.get(0));
        });

        // 不只简单处理为json
        response.json(entity.getBody());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 500;
    }
}