package sky.starter.domain;

import org.springframework.web.method.HandlerMethod;

/**
 * sky route definition info.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 15:26
 */
public class SkyRouteDefinition {

    private SkyHandlerInfo handlerInfo;

    private HandlerMethod handlerMethod;

    public SkyRouteDefinition(final SkyHandlerInfo handlerInfo, final HandlerMethod handlerMethod) {
        this.handlerInfo = handlerInfo;
        this.handlerMethod = handlerMethod;
    }
}