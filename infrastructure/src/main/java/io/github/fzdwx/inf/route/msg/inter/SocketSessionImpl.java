package io.github.fzdwx.inf.route.msg.inter;

import io.github.fzdwx.inf.route.msg.SocketSession;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 20:18
 */
public class SocketSessionImpl implements SocketSession {

    private final Channel channel;
    private final FullHttpRequest request;

    public SocketSessionImpl(final Channel channel, final FullHttpRequest request) {
        this.channel = channel;
        this.request = request;
    }

    @Override
    public Channel channel() {
        return this.channel;
    }
}