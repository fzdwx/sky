package sky.starter.ext;

/**
 * val convertor.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/21 22:12
 */
public interface ValConvertor {

    <T> T convert(Object val, T defaultVal, Class<T> clazz);

    default <T> T convert(Object val, Class<T> clazz) {
        return convert(val, null, clazz);
    }
}