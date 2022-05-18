package sky.starter.resphandler;

import http.HttpServerResponse;
import sky.starter.domain.SkyRouteDefinition;
import sky.starter.ext.RequestResultHandler;

/**
 * support for {@link org.springframework.web.bind.annotation.ResponseBody}
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 17:18
 */
public class ResponseBodyRequestResultHandler implements RequestResultHandler {

    @Override
    public boolean support(final Object result, final SkyRouteDefinition definition) {
        return definition.enableJson();
    }

    @Override
    public void apply(final Object result, final SkyRouteDefinition definition, final HttpServerResponse response) {
        response.json(result);
    }
}