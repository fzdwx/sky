package http;

import core.Netty;
import core.NettyOutbound;
import http.inter.HttpServerResponseImpl;
import http.util.ContentType;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.Charset;

/**
 * http response.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 15:23
 * @since 0.06
 */
public interface HttpServerResponse extends NettyOutbound {

    static HttpServerResponse create(Channel channel, final HttpServerRequest httpRequest) {
        return new HttpServerResponseImpl(channel, httpRequest);
    }

    Channel channel();

    /**
     * http headers
     */
    HttpHeaders headers();

    /**
     * is end of response.
     */
    boolean isEnd();

    /**
     * http version
     */
    HttpVersion version();

    HttpServerResponse status(HttpResponseStatus status);

    HttpServerResponse keepAlive(boolean keepAlive);

    HttpServerResponse contentType(final String contentType);

    HttpServerResponse contentType(final ContentType contentType);

    HttpServerResponse contentDisposition(final String contentDisposition);

    HttpServerResponse contentDispositionFull(String contentDisposition);

    /**
     * mount 当消息体发送完毕时会回调.
     *
     * @param endH end handler
     * @return {@link HttpServerResponse }
     */
    HttpServerResponse mountBodyEnd(Hooks<Void> endH);

    /**
     * 如果chunked为true ，则此响应将使用 HTTP 分块编码，并且每次写入正文的调用都将对应于在线发送的新 HTTP 块。
     * 如果使用分块编码，则 HTTP 标头Transfer-Encoding的值为Chunked将自动插入响应中。
     * 如果chunked为false ，则此响应将不使用 HTTP 分块编码，因此写入响应正文中的任何数据的总大小必须在任何数据写出之前在Content-Length标头中设置。
     * 当您预先不知道请求正文的总大小时，通常会使用 HTTP 分块响应。
     */
    HttpServerResponse chunked();

    /**
     * {@link #chunked()}
     */
    HttpServerResponse unChunked();

    boolean isChunked();

    default ChannelFuture notFound(final String message) {
        return this.status(HttpResponseStatus.NOT_FOUND)
                .end(message);
    }

    default ChannelFuture notFound() {
        return this.status(HttpResponseStatus.NOT_FOUND)
                .end();
    }

    /**
     * add header.
     */
    HttpServerResponse header(CharSequence key, CharSequence val);

    /**
     * add cookie.
     */
    HttpServerResponse cookie(String key, String val);

    /**
     * write buf to client.
     */
    ChannelFuture write(ByteBuf buf);

    ChannelFuture reject();

    /**
     * @return
     * @since 0.07
     */
    ChannelFuture redirect(String url);

    /**
     * out put json data to client.
     *
     * @apiNote auto flush and close
     */
    ChannelFuture json(final Object obj);

    /**
     * @apiNote auto flush and close
     */
    ChannelFuture html(String html);

    ChannelFuture end(ByteBuf buf);

    void close();

    default ChannelFuture end(byte[] bytes) {
        return end(Netty.wrap(bytes));
    }

    default ChannelFuture end() {
        return end(Unpooled.EMPTY_BUFFER);
    }

    default ChannelFuture end(String s) {
        return end(Netty.wrap(s.getBytes()));
    }

    default HttpServerResponse end(String s, Hooks<ChannelFuture> h) {
        h.call(end(Netty.wrap(s.getBytes())));
        return this;
    }

    /**
     * @since 0.07
     */
    default void json(final Object obj, Hooks<ChannelFuture> h) {
        h.call(json(obj));
    }

    /**
     * @since 0.07
     */
    default void html(String html, Hooks<ChannelFuture> h) {
        h.call(html(html));
    }

    default HttpServerResponse write(ByteBuf buf, Hooks<ChannelFuture> h) {
        h.call(write(buf));
        return this;
    }

    default ChannelFuture write(byte[] bytes) {
        return write(Netty.wrap(bytes));
    }

    default HttpServerResponse write(byte[] bytes, Hooks<ChannelFuture> h) {
        h.call(write(bytes));
        return this;
    }

    default ChannelFuture write(String s, Charset charset) {
        return write(s.getBytes(charset));
    }

    default HttpServerResponse write(String s, Charset charset, Hooks<ChannelFuture> h) {
        h.call(write(s, charset));
        return this;
    }

    default ChannelFuture write(String s) {
        return write(s, Lang.CHARSET);
    }

    default ChannelFuture writeFlush(String s) {
        return sendAndFlush(s.getBytes(Lang.CHARSET)).then();
    }

    default HttpServerResponse write(String s, Hooks<ChannelFuture> h) {
        h.call(write(s));
        return this;
    }
}