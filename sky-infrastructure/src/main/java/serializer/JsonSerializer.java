package serializer;

import core.Netty;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/21 17:58
 */
public interface JsonSerializer extends Serializer {

    JsonSerializer codec = FasJsonSerializerSerializer.instance;

    default ByteBuf encodeToBuf(final Object obj) {
        return Netty.wrap(serialize(obj));
    }
}