package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.HttpResponse;
import io.netty.channel.Channel;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:27
 */
public class HttpResponseImpl implements HttpResponse {

    public final Channel channel;

    public HttpResponseImpl(final Channel channel) {
        this.channel = channel;
    }

    @Override
    public void json(final String json) {
        channel.writeAndFlush(HttpResult.ok(json)).addListener(Netty.close);
    }

    @Override
    public void json(final byte[] json) {
        channel.writeAndFlush(HttpResult.ok(json).json()).addListener(Netty.close);
    }

    @Override
    public void html(final String html) {
        channel.writeAndFlush(HttpResult.ok(html)).addListener(Netty.close);
    }

    @Override
    public void bytes(final byte[] bytes) {
        channel.writeAndFlush(HttpResult.ok(bytes)).addListener(Netty.close);
    }
}