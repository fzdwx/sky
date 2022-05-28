package core.serializer;

import util.Netty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/21 17:58
 */
public interface JsonSerializer extends Serializer {

    JsonSerializer codec = FasJsonSerializerSerializer.instance;

    <T> T toBean(String json, Class<T> targetClass);

    default ByteBuf encodeToBuf(final ByteBufAllocator alloc, final Object o) {
        return Netty.wrap(alloc, serialize(o));
    }
}