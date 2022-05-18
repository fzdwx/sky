package sky.starter.ext;

import http.HttpServerResponse;
import sky.starter.domain.SkyRouteDefinition;

/**
 * request result handler.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 17:01
 */
public interface RequestResultHandler {

    boolean support(Object result, SkyRouteDefinition definition);

    void apply(Object result, SkyRouteDefinition definition, HttpServerResponse response);
}