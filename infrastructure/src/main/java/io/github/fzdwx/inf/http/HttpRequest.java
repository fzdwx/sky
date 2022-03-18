package io.github.fzdwx.inf.http;

import io.github.fzdwx.inf.Listener;
import io.github.fzdwx.inf.http.inter.HttpRequestImpl;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 19:57
 */
public interface HttpRequest {

    static HttpRequest create(ChannelHandlerContext ctx, FullHttpRequest request) {
        return new HttpRequestImpl(ctx, request);
    }

    String uri();

    RequestMethod methodType();

    /**
     * handler websocket request.
     *
     * @param listener 侦听器
     */
    void upgradeToWebSocket(final Listener listener);
}