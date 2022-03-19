package io.github.fzdwx.inf.route.msg;

import io.github.fzdwx.inf.route.msg.inter.SocketSessionImpl;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * session.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 13:11
 */
public interface SocketSession {

    Channel channel();

    static SocketSession create(Channel channel, FullHttpRequest request) {
        return new SocketSessionImpl(channel, request);
    }

    void send(String text);

    void send(byte[] text);

    void sendBinary(byte[] binary);
}