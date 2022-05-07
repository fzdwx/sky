package ser;


import com.alibaba.fastjson2.JSON;

/**
 * fastjson serializer.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/17 17:28
 * @since 0.06
 */
public class FasJsonSerializer implements Json {

    public static FasJsonSerializer instance = new FasJsonSerializer();

    @Override
    public byte[] serialize(final Object obj) {
        return JSON.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(final Class<T> clazz, final byte[] data) {
        return JSON.parseObject(data, clazz);
    }
}