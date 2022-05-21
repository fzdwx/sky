package sky.starter.bean;

import cn.hutool.core.convert.ConverterRegistry;
import sky.starter.ext.ValConvertor;

/**
 * default(use hutool) val convertor
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/21 22:14
 */
public class DefaultValConvertor implements ValConvertor {

    final ConverterRegistry registry = ConverterRegistry.getInstance();

    @Override
    public <T> T convert(final Object val, final T defaultVal, final Class<T> clazz) {
        return registry.convert(clazz, val, defaultVal);
    }
}