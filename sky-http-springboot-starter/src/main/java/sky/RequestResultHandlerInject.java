package sky;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import sky.starter.resphandler.ResponseBodyRequestResultHandler;

/**
 * inject all {@link sky.starter.ext.RequestResultHandler}
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 17:21
 */
public class RequestResultHandlerInject {

    @Bean
    @ConditionalOnMissingBean
    ResponseBodyRequestResultHandler responseBodyRequestResultHandler() {
        return new ResponseBodyRequestResultHandler();
    }
}