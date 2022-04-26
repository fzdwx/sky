package org.atomicode.inf.ser;

import io.netty.buffer.ByteBuf;
import org.atomicode.inf.Netty;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/21 17:58
 */
public interface Json extends Serializer {

    Json codec = FasJsonSerializer.instance;

    default ByteBuf encodeToBuf(final Object obj) {
        return Netty.wrap(serialize(obj));
    }
}