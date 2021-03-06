package core.serializer;

/**
 * serializer.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/17 17:27
 * @since 0.06
 */
public interface Serializer {

    Serializer DEFAULT = JsonSerializer.codec;

    /**
     * @since 0.06
     */
    byte[] serialize(Object obj);

    /**
     * @since 0.06
     */
    <T> T deserialize(Class<T> clazz, byte[] data);
}