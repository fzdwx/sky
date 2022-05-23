package serializer;


import com.alibaba.fastjson2.JSON;

/**
 * fastjson serializer.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/17 17:28
 * @since 0.06
 */
public class FasJsonSerializerSerializer implements JsonSerializer {

    public static FasJsonSerializerSerializer instance = new FasJsonSerializerSerializer();

    @Override
    public byte[] serialize(final Object obj) {
        return JSON.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(final Class<T> clazz, final byte[] data) {
        return JSON.parseObject(data, clazz);
    }

    @Override
    public <T> T toBean(final String json, final Class<T> targetClass) {
        return JSON.parseObject(json, targetClass);
    }
}