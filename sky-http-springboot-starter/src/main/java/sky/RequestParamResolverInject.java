package sky;

import io.github.fzdwx.lambada.Collections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import sky.starter.ext.PathVariableRequestParamResolver;
import sky.starter.ext.RequestParamResolver;

import java.util.List;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/18 21:32
 */
public class RequestParamResolverInject {

    @Bean
    @ConditionalOnMissingBean
    List<RequestParamResolver> paramResolvers() {
        return Collections.list(
                new PathVariableRequestParamResolver()
        );
    }

}