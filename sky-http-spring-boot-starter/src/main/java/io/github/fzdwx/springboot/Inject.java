package io.github.fzdwx.springboot;

import io.github.fzdwx.RequestMounter;
import io.github.fzdwx.inf.route.Router;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 14:36
 */
@Component
@RequiredArgsConstructor
public class Inject implements BeanFactoryPostProcessor, ApplicationContextAware {

    private final Router router;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (final RequestMounter bean : beanFactory.getBeansOfType(RequestMounter.class).values()) {
            bean.mount(router);
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

    }
}