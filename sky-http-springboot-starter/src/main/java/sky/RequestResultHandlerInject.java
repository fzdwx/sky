package sky;

import io.github.fzdwx.lambada.Collections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import sky.starter.ext.RequestResultHandler;
import sky.starter.resphandler.EveryRequestResultHandler;
import sky.starter.resphandler.ResponseBodyRequestResultHandler;

import java.util.List;

/**
 * inject all {@link sky.starter.ext.RequestResultHandler}
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 17:21
 */
public class RequestResultHandlerInject {

    @Bean
    @ConditionalOnMissingBean
    List<RequestResultHandler> resultHandlers() {
        return Collections.list(
                new ResponseBodyRequestResultHandler(),
                new EveryRequestResultHandler()
        );
    }
}