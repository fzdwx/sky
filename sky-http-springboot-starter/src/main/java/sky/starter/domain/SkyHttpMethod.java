package sky.starter.domain;

import io.github.fzdwx.lambada.anno.NonNull;
import io.github.fzdwx.lambada.anno.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * copy from spring,but modify some method.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @apiNote <pre>
 *     1. add methodParameterName
 * </pre>
 * @date 2022/5/18 21:55
 */
@Slf4j
public class SkyHttpMethod {

    private final Object bean;

    @Nullable
    private final BeanFactory beanFactory;

    @Nullable
    private final MessageSource messageSource;

    private final Class<?> beanType;

    private final Method method;

    private final Method bridgedMethod;

    private final SkyHttpMethodParameter[] parameters;
    private final String description;
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    @Nullable
    private HttpStatus responseStatus;
    @Nullable
    private String responseStatusReason;
    @Nullable
    private SkyHttpMethod resolvedFromSkyHttpMethod;
    @Nullable
    private volatile List<Annotation[][]> interfaceParameterAnnotations;

    /**
     * Create an instance from a bean instance and a method.
     */
    public SkyHttpMethod(Object bean, Method method) {
        this(bean, method, null);
    }

    /**
     * Variant of {@link #SkyHttpMethod(Object, Method)} that
     * also accepts a {@link MessageSource} for use from sub-classes.
     *
     * @since 5.3.10
     */
    protected SkyHttpMethod(Object bean, Method method, @Nullable MessageSource messageSource) {
        Assert.notNull(bean, "Bean is required");
        Assert.notNull(method, "Method is required");
        this.bean = bean;
        this.beanFactory = null;
        this.messageSource = messageSource;
        this.beanType = ClassUtils.getUserClass(bean);
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
        ReflectionUtils.makeAccessible(this.bridgedMethod);
        this.parameters = initMethodParameters();
        evaluateResponseStatus();
        this.description = initDescription(this.beanType, this.method);
    }

    /**
     * Create an instance from a bean instance, method name, and parameter types.
     *
     * @throws NoSuchMethodException when the method cannot be found
     */
    public SkyHttpMethod(Object bean, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Assert.notNull(bean, "Bean is required");
        Assert.notNull(methodName, "Method name is required");
        this.bean = bean;
        this.beanFactory = null;
        this.messageSource = null;
        this.beanType = ClassUtils.getUserClass(bean);
        this.method = bean.getClass().getMethod(methodName, parameterTypes);
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(this.method);
        ReflectionUtils.makeAccessible(this.bridgedMethod);
        this.parameters = initMethodParameters();
        evaluateResponseStatus();
        this.description = initDescription(this.beanType, this.method);
    }

    /**
     * Create an instance from a bean name, a method, and a {@code BeanFactory}.
     * The method {@link #createWithResolvedBean()} may be used later to
     * re-create the {@code SkyHttpMethod} with an initialized bean.
     */
    public SkyHttpMethod(String beanName, BeanFactory beanFactory, Method method) {
        this(beanName, beanFactory, null, method);
    }

    /**
     * Variant of {@link #SkyHttpMethod(String, BeanFactory, Method)} that
     * also accepts a {@link MessageSource}.
     */
    public SkyHttpMethod(
            String beanName, BeanFactory beanFactory,
            @Nullable MessageSource messageSource, Method method) {
        Assert.hasText(beanName, "Bean name is required");
        Assert.notNull(beanFactory, "BeanFactory is required");
        Assert.notNull(method, "Method is required");
        this.bean = beanName;
        this.beanFactory = beanFactory;
        this.messageSource = messageSource;
        Class<?> beanType = beanFactory.getType(beanName);
        if (beanType == null) {
            throw new IllegalStateException("Cannot resolve bean type for bean with name '" + beanName + "'");
        }
        this.beanType = ClassUtils.getUserClass(beanType);
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
        ReflectionUtils.makeAccessible(this.bridgedMethod);
        this.parameters = initMethodParameters();
        evaluateResponseStatus();
        this.description = initDescription(this.beanType, this.method);
    }

    /**
     * Copy constructor for use in subclasses.
     */
    protected SkyHttpMethod(SkyHttpMethod SkyHttpMethod) {
        Assert.notNull(SkyHttpMethod, "SkyHttpMethod is required");
        this.bean = SkyHttpMethod.bean;
        this.beanFactory = SkyHttpMethod.beanFactory;
        this.messageSource = SkyHttpMethod.messageSource;
        this.beanType = SkyHttpMethod.beanType;
        this.method = SkyHttpMethod.method;
        this.bridgedMethod = SkyHttpMethod.bridgedMethod;
        this.parameters = SkyHttpMethod.parameters;
        this.responseStatus = SkyHttpMethod.responseStatus;
        this.responseStatusReason = SkyHttpMethod.responseStatusReason;
        this.description = SkyHttpMethod.description;
        this.resolvedFromSkyHttpMethod = SkyHttpMethod.resolvedFromSkyHttpMethod;
    }

    /**
     * Re-create SkyHttpMethod with the resolved handler.
     */
    private SkyHttpMethod(SkyHttpMethod SkyHttpMethod, Object handler) {
        Assert.notNull(SkyHttpMethod, "SkyHttpMethod is required");
        Assert.notNull(handler, "Handler object is required");
        this.bean = handler;
        this.beanFactory = SkyHttpMethod.beanFactory;
        this.messageSource = SkyHttpMethod.messageSource;
        this.beanType = SkyHttpMethod.beanType;
        this.method = SkyHttpMethod.method;
        this.bridgedMethod = SkyHttpMethod.bridgedMethod;
        this.parameters = SkyHttpMethod.parameters;
        this.responseStatus = SkyHttpMethod.responseStatus;
        this.responseStatusReason = SkyHttpMethod.responseStatusReason;
        this.resolvedFromSkyHttpMethod = SkyHttpMethod;
        this.description = SkyHttpMethod.description;
    }

    /**
     * Return the bean for this handler method.
     */
    public Object getBean() {
        return this.bean;
    }

    /**
     * Return the method for this handler method.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * This method returns the type of the handler for this handler method.
     * <p>Note that if the bean type is a CGLIB-generated class, the original
     * user-defined class is returned.
     */
    public Class<?> getBeanType() {
        return this.beanType;
    }

    /**
     * Return the method parameters for this handler method.
     */
    public SkyHttpMethodParameter[] getMethodParameters() {
        return this.parameters;
    }

    /**
     * Return the SkyHttpMethod return type.
     */
    public MethodParameter getReturnType() {
        return new ReturnValueMethodParameter(-1);
    }

    /**
     * Return the actual return value type.
     */
    public MethodParameter getReturnValueType(@Nullable Object returnValue) {
        return new ReturnValueMethodParameter(returnValue);
    }

    /**
     * Return {@code true} if the method return type is void, {@code false} otherwise.
     */
    public boolean isVoid() {
        return Void.TYPE.equals(getReturnType().getParameterType());
    }

    @Nullable
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
        return AnnotatedElementUtils.findMergedAnnotation(this.method, annotationType);
    }


    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
        return AnnotatedElementUtils.hasAnnotation(this.method, annotationType);
    }

    public SkyHttpMethod createWithResolvedBean() {
        Object handler = this.bean;
        if (this.bean instanceof String) {
            Assert.state(this.beanFactory != null, "Cannot resolve bean name without BeanFactory");
            String beanName = (String) this.bean;
            handler = this.beanFactory.getBean(beanName);
        }
        return new SkyHttpMethod(this, handler);
    }

    /**
     * Return a short representation of this handler method for log message purposes.
     *
     * @since 4.3
     */
    public String getShortLogMessage() {
        return getBeanType().getName() + "#" + this.method.getName() +
                "[" + this.method.getParameterCount() + " args]";
    }

    @Override
    public int hashCode() {
        return (this.bean.hashCode() * 31 + this.method.hashCode());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SkyHttpMethod)) {
            return false;
        }
        SkyHttpMethod otherMethod = (SkyHttpMethod) other;
        return (this.bean.equals(otherMethod.bean) && this.method.equals(otherMethod.method));
    }

    @Override
    public String toString() {
        return this.description;
    }

    /**
     * Return the SkyHttpMethod from which this SkyHttpMethod instance was
     * resolved via {@link #createWithResolvedBean()}.
     */
    @Nullable
    public SkyHttpMethod getResolvedFromSkyHttpMethod() {
        return this.resolvedFromSkyHttpMethod;
    }

    /**
     * If the bean method is a bridge method, this method returns the bridged
     * (user-defined) method. Otherwise it returns the same method as {@link #getMethod()}.
     */
    protected Method getBridgedMethod() {
        return this.bridgedMethod;
    }

    /**
     * Return the specified response status, if any.
     *
     * @see ResponseStatus#code()
     * @since 4.3.8
     */
    @Nullable
    protected HttpStatus getResponseStatus() {
        return this.responseStatus;
    }

    /**
     * Return the associated response status reason, if any.
     *
     * @see ResponseStatus#reason()
     * @since 4.3.8
     */
    @Nullable
    protected String getResponseStatusReason() {
        return this.responseStatusReason;
    }

    /**
     * Assert that the target bean class is an instance of the class where the given
     * method is declared. In some cases the actual controller instance at request-
     * processing time may be a JDK dynamic proxy (lazy initialization, prototype
     * beans, and others). {@code @Controller}'s that require proxying should prefer
     * class-based proxy mechanisms.
     */
    protected void assertTargetBean(Method method, Object targetBean, Object[] args) {
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        Class<?> targetBeanClass = targetBean.getClass();
        if (!methodDeclaringClass.isAssignableFrom(targetBeanClass)) {
            String text = "The mapped handler method class '" + methodDeclaringClass.getName() +
                    "' is not an instance of the actual controller bean class '" +
                    targetBeanClass.getName() + "'. If the controller requires proxying " +
                    "(e.g. due to @Transactional), please use class-based proxying.";
            throw new IllegalStateException(formatInvokeError(text, args));
        }
    }

    protected String formatInvokeError(String text, Object[] args) {
        String formattedArgs = IntStream.range(0, args.length)
                .mapToObj(i -> (args[i] != null ?
                        "[" + i + "] [type=" + args[i].getClass().getName() + "] [value=" + args[i] + "]" :
                        "[" + i + "] [null]"))
                .collect(Collectors.joining(",\n", " ", " "));
        return text + "\n" +
                "Controller [" + getBeanType().getName() + "]\n" +
                "Method [" + getBridgedMethod().toGenericString() + "] " +
                "with argument values:\n" + formattedArgs;
    }

    @Nullable
    protected static Object findProvidedArgument(MethodParameter parameter, @Nullable Object... providedArgs) {
        if (!ObjectUtils.isEmpty(providedArgs)) {
            for (Object providedArg : providedArgs) {
                if (parameter.getParameterType().isInstance(providedArg)) {
                    return providedArg;
                }
            }
        }
        return null;
    }

    protected static String formatArgumentError(MethodParameter param, String message) {
        return "Could not resolve parameter [" + param.getParameterIndex() + "] in " +
                param.getExecutable().toGenericString() + (StringUtils.hasText(message) ? ": " + message : "");
    }

    private SkyHttpMethodParameter[] initMethodParameters() {
        int count = this.bridgedMethod.getParameterCount();
        SkyHttpMethodParameter[] result = new SkyHttpMethodParameter[count];
        for (int i = 0; i < count; i++) {
            final SkyHttpMethodParameter parameter = new SkyHttpMethodParameter(i);
            // ext
            result[i] = parameter;
        }
        return result;
    }

    private void evaluateResponseStatus() {
        ResponseStatus annotation = getMethodAnnotation(ResponseStatus.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(getBeanType(), ResponseStatus.class);
        }
        if (annotation != null) {
            String reason = annotation.reason();
            String resolvedReason = (StringUtils.hasText(reason) && this.messageSource != null ?
                    this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale()) :
                    reason);

            this.responseStatus = annotation.code();
            this.responseStatusReason = resolvedReason;
        }
    }

    private List<Annotation[][]> getInterfaceParameterAnnotations() {
        List<Annotation[][]> parameterAnnotations = this.interfaceParameterAnnotations;
        if (parameterAnnotations == null) {
            parameterAnnotations = new ArrayList<>();
            for (Class<?> ifc : ClassUtils.getAllInterfacesForClassAsSet(this.method.getDeclaringClass())) {
                for (Method candidate : ifc.getMethods()) {
                    if (isOverrideFor(candidate)) {
                        parameterAnnotations.add(candidate.getParameterAnnotations());
                    }
                }
            }
            this.interfaceParameterAnnotations = parameterAnnotations;
        }
        return parameterAnnotations;
    }

    private boolean isOverrideFor(Method candidate) {
        if (!candidate.getName().equals(this.method.getName()) ||
                candidate.getParameterCount() != this.method.getParameterCount()) {
            return false;
        }
        Class<?>[] paramTypes = this.method.getParameterTypes();
        if (Arrays.equals(candidate.getParameterTypes(), paramTypes)) {
            return true;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] !=
                    ResolvableType.forMethodParameter(candidate, i, this.method.getDeclaringClass()).resolve()) {
                return false;
            }
        }
        return true;
    }

    private static String initDescription(Class<?> beanType, Method method) {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        for (Class<?> paramType : method.getParameterTypes()) {
            joiner.add(paramType.getSimpleName());
        }
        return beanType.getName() + "#" + method.getName() + joiner.toString();
    }

    public class SkyHttpMethodParameter extends SynthesizingMethodParameter {

        private final String parameterName;
        @Nullable
        private volatile Annotation[] combinedAnnotations;

        @Getter
        @Nullable
        private PathVariable pathVariable;
        @Getter
        @Nullable
        private Map<String, Object> pathVariableAttrs;

        @Getter
        @Nullable
        private RequestBody requestBody;
        @Getter
        @Nullable
        private Map<String, Object> requestBodyAttr;

        @Getter
        @Nullable
        private RequestParam requestParam;
        @Getter
        @Nullable
        private Map<String, Object> requestParamAttr;

        public SkyHttpMethodParameter(int index) {
            super(SkyHttpMethod.this.bridgedMethod, index);
            initParameterNameDiscovery(parameterNameDiscoverer);
            parameterName = super.getParameterName();
            initPathVariable();
            initRequestBody();
            initRequestParam();
        }

        private void initRequestParam() {
            this.requestParam = getParameter().getAnnotation(RequestParam.class);
            if (this.requestParam == null) {
                return;
            }
            this.requestParamAttr = AnnotationUtils.getAnnotationAttributes(requestParam);
        }

        private void initRequestBody() {
            this.requestBody = getParameter().getAnnotation(RequestBody.class);
            if (this.requestBody == null) {
                return;
            }
            this.requestBodyAttr = AnnotationUtils.getAnnotationAttributes(requestBody);
        }

        private void initPathVariable() {
            this.pathVariable = getParameter().getAnnotation(PathVariable.class);
            if (this.pathVariable == null) {
                return;
            }
            this.pathVariableAttrs = AnnotationUtils.getAnnotationAttributes(pathVariable);
        }

        protected SkyHttpMethodParameter(SkyHttpMethodParameter original) {
            super(original);
            initParameterNameDiscovery(parameterNameDiscoverer);
            parameterName = super.getParameterName();
        }

        @Override
        @NonNull
        public Method getMethod() {
            return SkyHttpMethod.this.bridgedMethod;
        }

        @Override
        public Class<?> getContainingClass() {
            return SkyHttpMethod.this.getBeanType();
        }

        @Override
        public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
            return SkyHttpMethod.this.getMethodAnnotation(annotationType);
        }

        @Override
        public <T extends Annotation> boolean hasMethodAnnotation(Class<T> annotationType) {
            return SkyHttpMethod.this.hasMethodAnnotation(annotationType);
        }

        @Override
        public Annotation[] getParameterAnnotations() {
            Annotation[] anns = this.combinedAnnotations;
            if (anns == null) {
                anns = super.getParameterAnnotations();
                int index = getParameterIndex();
                if (index >= 0) {
                    for (Annotation[][] ifcAnns : getInterfaceParameterAnnotations()) {
                        if (index < ifcAnns.length) {
                            Annotation[] paramAnns = ifcAnns[index];
                            if (paramAnns.length > 0) {
                                List<Annotation> merged = new ArrayList<>(anns.length + paramAnns.length);
                                merged.addAll(Arrays.asList(anns));
                                for (Annotation paramAnn : paramAnns) {
                                    boolean existingType = false;
                                    for (Annotation ann : anns) {
                                        if (ann.annotationType() == paramAnn.annotationType()) {
                                            existingType = true;
                                            break;
                                        }
                                    }
                                    if (!existingType) {
                                        merged.add(adaptAnnotation(paramAnn));
                                    }
                                }
                                anns = merged.toArray(new Annotation[0]);
                            }
                        }
                    }
                }
                this.combinedAnnotations = anns;
            }
            return anns;
        }

        @Override
        @NonNull
        public String getParameterName() {
            return this.parameterName;
        }

        @Override
        public SkyHttpMethodParameter clone() {
            return new SkyHttpMethodParameter(this);
        }
    }

    public class ReturnValueMethodParameter extends SkyHttpMethodParameter {

        @Nullable
        private final Class<?> returnValueType;

        public ReturnValueMethodParameter(@Nullable Object returnValue) {
            super(-1);
            this.returnValueType = (returnValue != null ? returnValue.getClass() : null);
        }

        protected ReturnValueMethodParameter(ReturnValueMethodParameter original) {
            super(original);
            this.returnValueType = original.returnValueType;
        }

        @Override
        public Class<?> getParameterType() {
            return (this.returnValueType != null ? this.returnValueType : super.getParameterType());
        }

        @Override
        public ReturnValueMethodParameter clone() {
            return new ReturnValueMethodParameter(this);
        }
    }
}