package sky.starter.domain;

import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.anno.NonNull;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.InvocationTargetException;

/**
 * sky route definition info.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 15:26
 */
public class SkyRouteDefinition {

    private SkyHandlerInfo handlerInfo;

    private HandlerMethod handlerMethod;

    public SkyRouteDefinition(@NonNull final SkyHandlerInfo handlerInfo, @NonNull final HandlerMethod handlerMethod) {
        Assert.nonNull(handlerInfo, "handlerInfo must not be null");
        Assert.nonNull(handlerMethod, "handlerMethod must not be null");

        this.handlerInfo = handlerInfo;
        this.handlerMethod = handlerMethod;
    }

    public HandlerMethod method() {
        return handlerMethod;
    }

    public void createWithResolvedBean() {
        this.handlerMethod = handlerMethod.createWithResolvedBean();
    }

    public boolean enableJson() {
        return handlerInfo.enableJson();
    }

    public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return handlerMethod.getMethod().invoke(handlerMethod.getBean(), args);
    }
}