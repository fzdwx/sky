package io.github.fzdwx.inf.http;

import io.github.fzdwx.inf.http.inter.HttpResponseImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * http response.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:23
 */
public interface HttpResponse {

    static HttpResponse create(Channel channel) {
        return new HttpResponseImpl(channel);
    }

    void json(final String json);

    void json(final byte[] json);

    void html(String html);

    void bytes(byte[] bytes);
}