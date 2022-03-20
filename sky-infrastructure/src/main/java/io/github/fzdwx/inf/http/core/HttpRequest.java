package io.github.fzdwx.inf.http.core;

import io.github.fzdwx.inf.http.inter.HttpRequestImpl;
import io.github.fzdwx.inf.msg.WebSocket;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.fun.Result;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * http request.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 19:57
 * @since 0.06
 */
public interface HttpRequest {

    static HttpRequest create(ChannelHandlerContext ctx, FullHttpRequest request) {
        return new HttpRequestImpl(ctx, request);
    }

    /**
     * request uri
     *
     * @return {@link String }
     */
    String uri();

    /**
     * request type
     *
     * @return {@link RequestMethod }
     */
    RequestMethod methodType();

    /**
     * accept websocket.
     */
    void upgradeToWebSocket(Hooks<WebSocket> h);

    Result<WebSocket> upgradeToWebSocket();
}