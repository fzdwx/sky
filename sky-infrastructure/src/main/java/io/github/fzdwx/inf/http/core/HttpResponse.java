package io.github.fzdwx.inf.http.core;

import io.github.fzdwx.inf.http.inter.HttpResponseImpl;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.File;

/**
 * http response.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:23
 * @since 0.06
 */
public interface HttpResponse {

    static HttpResponse create(Channel channel) {
        return new HttpResponseImpl(channel);
    }

    Channel channel();

    /**
     * out put json data to client.
     *
     * @apiNote auto close
     */
    void json(final String json);

    /**
     * @since 0.07
     */
    void json(final String json, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto close
     */
    void json(final byte[] json);

    /**
     * @since 0.07
     */
    void json(final byte[] json, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto close
     */
    void html(String html);

    /**
     * @since 0.07
     */
    void html(String html, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto close
     */
    void bytes(byte[] bytes);

    /**
     * @since 0.07
     */
    void bytes(byte[] bytes, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto close
     */
    void redirect(String url);

    /**
     * @since 0.07
     */
    void redirect(String url, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto close
     */
    void file(String filePath);

    /**
     * @since 0.07
     */
    void file(String filePath, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto close
     */
    void file(File file);

    /**
     * @since 0.07
     */
    void file(File file, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto close
     */
    void file(byte[] bytes, String fileName);

    /**
     * @since 0.07
     */
    void file(byte[] bytes, String fileName, Hooks<ChannelFuture> h);

    /**
     * output str to client.
     *
     * @apiNote auto close
     * @see ContentType
     * @see io.netty.handler.codec.http.HttpHeaderValues
     */
    void output(String str, String contentType);

    /**
     * @since 0.07
     */
    void output(String str, String contentType, Hooks<ChannelFuture> h);
}