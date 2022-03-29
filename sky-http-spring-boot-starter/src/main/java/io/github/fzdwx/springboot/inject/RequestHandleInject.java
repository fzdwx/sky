package io.github.fzdwx.springboot.inject;

import cn.hutool.core.annotation.AnnotationUtil;
import io.github.fzdwx.RequestMounter;
import io.github.fzdwx.inf.route.Router;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 14:36
 */
@RequiredArgsConstructor
public class RequestHandleInject implements BeanFactoryPostProcessor, ApplicationContextAware {

    private final Router router;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (final RequestMounter bean : beanFactory.getBeansOfType(RequestMounter.class).values()) {
            bean.mount(router);
        }

        beanFactory.getBeansWithAnnotation(Controller.class).forEach((name, bean) -> {
            final Controller connAnno = AnnotationUtil.getAnnotation(bean.getClass(), Controller.class);


        });

    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

    }
}