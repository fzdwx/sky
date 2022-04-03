package io.github.fzdwx.springboot.wrap;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/4/3 12:24
 */
@RequiredArgsConstructor
public class MethodWrap {

    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final Method method;
    private String[] argNames;

    public String[] getArgNames() {
        if (argNames == null) {
            this.argNames = parameterNameDiscoverer.getParameterNames(method);
        }
        return this.argNames;
    }

    public Parameter[] getParameters() {
        return method.getParameters();
    }

    public Object invoke(final Object source, final Object[] parseArgs) throws InvocationTargetException, IllegalAccessException {
        return this.method.invoke(source, parseArgs);
    }

    public Object invoke(final Object source) throws InvocationTargetException, IllegalAccessException {
        return this.method.invoke(source);
    }

    public AnnotatedElement getSource() {
        return method;
    }
}