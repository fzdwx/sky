package io.github.fzdwx.inf.http.core;

import io.github.fzdwx.inf.http.inter.HttpServerResponseImpl;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpMessage;

import java.io.File;
import java.io.InputStream;

import static io.github.fzdwx.inf.Netty.close;

/**
 * http response.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:23
 * @since 0.06
 */
public interface HttpServerResponse {

    static HttpServerResponse create(Channel channel) {
        return new HttpServerResponseImpl(channel);
    }

    Channel channel();

    HttpServerResponse contentType(final String contentType);

    HttpServerResponse contentDisposition(final String contentDisposition);

    HttpServerResponse contentDispositionFull(String contentDisposition);

    ChannelFuture reject();

    ChannelFuture reject(HttpMessage result);

    /**
     * @apiNote auto flush and close
     */
    void redirect(String url);

    /**
     * @since 0.07
     */
    void redirect(String url, Hooks<ChannelFuture> h);


    /**
     * out put json data to client.
     *
     * @apiNote auto flush and close
     */
    void json(final String json);

    /**
     * @since 0.07
     */
    void json(final String json, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto flush and close
     */
    void json(final byte[] json);

    /**
     * @since 0.07
     */
    void json(final byte[] json, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto flush and close
     */
    void html(String html);

    /**
     * @since 0.07
     */
    void html(String html, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto flush and close
     */
    void file(String filePath);

    /**
     * @since 0.07
     */
    void file(String filePath, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto flush and close
     */
    void file(File file);

    /**
     * @since 0.07
     */
    void file(File file, Hooks<ChannelFuture> h);

    /**
     * output string to client.
     *
     * @apiNote auto flush and close
     * @see #contentType
     * @see io.netty.handler.codec.http.HttpHeaderValues
     */
    void output(String str);

    /**
     * @see #contentType
     * @since 0.07
     */
    void output(String str, Hooks<ChannelFuture> h);

    /**
     * @apiNote auto flush and close
     */
    default void bytes(byte[] bytes) {
        bytes(bytes, c -> c.addListener(f -> channel().flush().close()));
    }

    /**
     * @since 0.07
     */
    void bytes(byte[] bytes, Hooks<ChannelFuture> h);

    /**
     * @param stream stream
     * @apiNote auto flush and close
     * @see #contentDisposition
     * @since 0.07
     */
    default void output(InputStream stream) {
        output(stream, f -> f.addListener(close));
    }

    /**
     * @see #contentDisposition
     * @since 0.07
     */
    void output(InputStream stream, Hooks<ChannelFuture> h);


    /**
     * output http result to client.
     *
     * @apiNote auto flush and close
     * @see ContentType
     * @see io.netty.handler.codec.http.HttpHeaderValues
     */
    default void output(HttpMessage result) {
        output(result, c -> c.addListener(f -> {
            channel().flush().close();
        }));
    }

    /**
     * @since 0.07
     */
    void output(HttpMessage result, Hooks<ChannelFuture> h);
}