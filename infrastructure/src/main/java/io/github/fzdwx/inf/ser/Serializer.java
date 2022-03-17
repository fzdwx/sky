package io.github.fzdwx.inf.ser;

/**
 * serializer.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 17:27
 */
public interface Serializer {

    byte[] serialize(Object obj);

    <T> T deserialize(Class<T> clazz, byte[] data);
}