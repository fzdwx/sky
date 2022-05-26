package sky.starter.ext;

import sky.starter.bean.RequestParamResolver;

/**
 * val convertor.
 * <p>
 * See {@link RequestParamResolver}
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/21 22:12
 * @see sky.starter.bean.DefaultValConvertor
 */
public interface ValConvertor {

    <T> T convert(Object val, T defaultVal, Class<T> clazz);

    default <T> T convert(Object val, Class<T> clazz) {
        return convert(val, null, clazz);
    }
}