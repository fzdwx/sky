package sky.starter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.metrics.ApplicationStartup;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 10:57
 */
public class SkyWebServerApplicationContext implements ConfigurableWebServerApplicationContext {

    @Override
    public void setServerNamespace(final String serverNamespace) {

    }

    @Override
    public WebServer getWebServer() {
        return null;
    }

    @Override
    public String getServerNamespace() {
        return null;
    }

    @Override
    public void setId(final String id) {

    }

    @Override
    public void setParent(final ApplicationContext parent) {

    }

    @Override
    public void setEnvironment(final ConfigurableEnvironment environment) {

    }

    @Override
    public ConfigurableEnvironment getEnvironment() {
        return null;
    }

    @Override
    public void setApplicationStartup(final ApplicationStartup applicationStartup) {

    }

    @Override
    public ApplicationStartup getApplicationStartup() {
        return null;
    }

    @Override
    public void addBeanFactoryPostProcessor(final BeanFactoryPostProcessor postProcessor) {

    }

    @Override
    public void addApplicationListener(final ApplicationListener<?> listener) {

    }

    @Override
    public void setClassLoader(final ClassLoader classLoader) {

    }

    @Override
    public void addProtocolResolver(final ProtocolResolver resolver) {

    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {

    }

    @Override
    public void registerShutdownHook() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public long getStartupDate() {
        return 0;
    }

    @Override
    public ApplicationContext getParent() {
        return null;
    }

    @Override
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return null;
    }

    @Override
    public BeanFactory getParentBeanFactory() {
        return null;
    }

    @Override
    public boolean containsLocalBean(final String name) {
        return false;
    }

    @Override
    public boolean containsBeanDefinition(final String beanName) {
        return false;
    }

    @Override
    public int getBeanDefinitionCount() {
        return 0;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return new String[0];
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(final Class<T> requiredType, final boolean allowEagerInit) {
        return null;
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(final ResolvableType requiredType, final boolean allowEagerInit) {
        return null;
    }

    @Override
    public String[] getBeanNamesForType(final ResolvableType type) {
        return new String[0];
    }

    @Override
    public String[] getBeanNamesForType(final ResolvableType type, final boolean includeNonSingletons, final boolean allowEagerInit) {
        return new String[0];
    }

    @Override
    public String[] getBeanNamesForType(final Class<?> type) {
        return new String[0];
    }

    @Override
    public String[] getBeanNamesForType(final Class<?> type, final boolean includeNonSingletons, final boolean allowEagerInit) {
        return new String[0];
    }

    @Override
    public <T> Map<String, T> getBeansOfType(final Class<T> type) throws BeansException {
        return null;
    }

    @Override
    public <T> Map<String, T> getBeansOfType(final Class<T> type, final boolean includeNonSingletons,
                                             final boolean allowEagerInit) throws BeansException {
        return null;
    }

    @Override
    public String[] getBeanNamesForAnnotation(final Class<? extends Annotation> annotationType) {
        return new String[0];
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(final Class<? extends Annotation> annotationType) throws BeansException {
        return null;
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(final String beanName, final Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return null;
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(final String beanName, final Class<A> annotationType,
                                                         final boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return null;
    }

    @Override
    public Object getBean(final String name) throws BeansException {
        return null;
    }

    @Override
    public <T> T getBean(final String name, final Class<T> requiredType) throws BeansException {
        return null;
    }

    @Override
    public Object getBean(final String name, final Object... args) throws BeansException {
        return null;
    }

    @Override
    public <T> T getBean(final Class<T> requiredType) throws BeansException {
        return null;
    }

    @Override
    public <T> T getBean(final Class<T> requiredType, final Object... args) throws BeansException {
        return null;
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(final Class<T> requiredType) {
        return null;
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(final ResolvableType requiredType) {
        return null;
    }

    @Override
    public boolean containsBean(final String name) {
        return false;
    }

    @Override
    public boolean isSingleton(final String name) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public boolean isPrototype(final String name) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public boolean isTypeMatch(final String name, final ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public boolean isTypeMatch(final String name, final Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public Class<?> getType(final String name) throws NoSuchBeanDefinitionException {
        return null;
    }

    @Override
    public Class<?> getType(final String name, final boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return null;
    }

    @Override
    public String[] getAliases(final String name) {
        return new String[0];
    }

    @Override
    public void publishEvent(final Object event) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public String getMessage(final String code, final Object[] args, final String defaultMessage, final Locale locale) {
        return null;
    }

    @Override
    public String getMessage(final String code, final Object[] args, final Locale locale) throws NoSuchMessageException {
        return null;
    }

    @Override
    public String getMessage(final MessageSourceResolvable resolvable, final Locale locale) throws NoSuchMessageException {
        return null;
    }

    @Override
    public Resource[] getResources(final String locationPattern) throws IOException {
        return new Resource[0];
    }

    @Override
    public Resource getResource(final String location) {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }
}