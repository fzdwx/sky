package io.github.fzdwx.inf.ser;

import com.alibaba.fastjson.JSON;

/**
 * fastjson serializer.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 17:28
 * @since 0.06
 */
public class FasJsonSerializer implements Serializer {

    @Override
    public byte[] serialize(final Object obj) {
        return JSON.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(final Class<T> clazz, final byte[] data) {
        return JSON.parseObject(data, clazz);
    }

    public static FasJsonSerializer instance = new FasJsonSerializer();

    public static byte[] encode(final Object obj) {
        return instance.serialize(obj);
    }

    public static <T> T decode(final Class<T> clazz, final byte[] data) {
        return instance.deserialize(clazz, data);
    }
}