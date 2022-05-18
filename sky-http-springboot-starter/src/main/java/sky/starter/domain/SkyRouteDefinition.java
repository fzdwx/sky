package sky.starter.domain;

import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.anno.NonNull;
import org.springframework.core.MethodParameter;
import sky.starter.ext.SkyHttpMethod;

import java.lang.reflect.InvocationTargetException;

/**
 * sky route definition info.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 15:26
 */
public class SkyRouteDefinition {

    private SkyHandlerInfo handlerInfo;

    private SkyHttpMethod handlerMethod;

    public SkyRouteDefinition(@NonNull final SkyHandlerInfo handlerInfo, @NonNull final SkyHttpMethod handlerMethod) {
        Assert.nonNull(handlerInfo, "handlerInfo must not be null");
        Assert.nonNull(handlerMethod, "handlerMethod must not be null");

        this.handlerInfo = handlerInfo;
        this.handlerMethod = handlerMethod;
    }

    public SkyHttpMethod method() {
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

    public MethodParameter[] getMethodParameters() {
        return this.handlerMethod.getMethodParameters();
    }
}