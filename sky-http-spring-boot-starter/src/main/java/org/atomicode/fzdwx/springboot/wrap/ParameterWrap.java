package org.atomicode.fzdwx.springboot.wrap;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/4/3 12:22
 */
@RequiredArgsConstructor
public class ParameterWrap {

    private final int idx;
    private final Parameter parameter;
    private final MethodWrap methodWrap;

    public String getArgName() {
        return methodWrap.getArgNames()[idx];
    }

    public Class<?> getType() {
        return parameter.getType();
    }

    public Parameter getSource() {
        return parameter;
    }
}