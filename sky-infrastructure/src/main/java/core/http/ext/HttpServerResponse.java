package core.http.ext;

import core.common.Outbound;
import core.http.inter.HttpServerResponseImpl;
import core.serializer.JsonSerializer;
import io.github.fzdwx.lambada.Assert;
import io.github.fzdwx.lambada.Io;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.http.ContentType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import util.Netty;

import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * http response.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 15:23
 * @since 0.06
 */
public interface HttpServerResponse extends Outbound {

    static HttpServerResponseImpl create(Channel channel, final HttpServerRequest httpRequest) {
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

    JsonSerializer serializer();

    /**
     * http version
     */
    HttpVersion version();

    /**
     * set  {@link  HttpResponseStatus}
     */
    HttpServerResponse status(HttpResponseStatus status);

    /**
     * add header keep-alive
     */
    HttpServerResponse keepAlive(boolean keepAlive);

    /**
     * add header contentType
     *
     * @see ContentType
     */
    HttpServerResponse contentType(final String contentType);

    /**
     * add header contentType
     *
     * @see ContentType
     */
    HttpServerResponse contentType(final ContentType contentType);

    /**
     * add header contentDisposition(attachment; filename=${fileName})
     *
     * @apiNote application scenario: return files to the client
     */
    HttpServerResponse contentDisposition(final String fileName);

    /**
     * add header contentDisposition(full)
     *
     * @apiNote application scenario: return files to the client
     */
    HttpServerResponse contentDispositionFull(String contentDisposition);

    /**
     * Mount the callback when the response is written...
     *
     * @param endH end handler
     * @return {@link HttpServerResponse }
     */
    HttpServerResponse mountBodyEnd(Hooks<Void> endH);

    /**
     * add header  {@code transfer-encoding} : chunked
     */
    HttpServerResponse chunked();

    /**
     * remove header {@code transfer-encoding}
     */
    HttpServerResponse unChunked();

    boolean isChunked();

    /**
     * return 404 response and write message
     */
    default ChannelFuture notFound(final String message) {
        return this.status(HttpResponseStatus.NOT_FOUND)
                .end(message);
    }

    /**
     * return 404 response
     */
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
     * reject request
     *
     * @apiNote just close channel
     */
    ChannelFuture reject();

    /**
     * redirect to the specified url
     *
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

    /**
     * end of the request.
     */
    ChannelFuture end(ByteBuf buf);

    /**
     * send file
     *
     * @throws IllegalArgumentException when file not found
     * @see #sendFile(RandomAccessFile, int, boolean, ChannelProgressiveFutureListener)
     */
    default ChannelFuture sendFile(String filePath) throws IllegalArgumentException {
        final RandomAccessFile file = Io.newRaf(filePath);
        Assert.nonNull(file, "file not found: " + filePath);

        return sendFile(file);
    }

    /**
     * @see #end(ByteBuf)
     */
    default ChannelFuture end(byte[] bytes) {
        return end(Netty.wrap(alloc(), bytes));
    }

    /**
     * @see #end(ByteBuf)
     */
    default ChannelFuture end() {
        return end(Unpooled.EMPTY_BUFFER);
    }

    /**
     * @see #end(ByteBuf)
     */
    default ChannelFuture end(String s) {
        return end(Netty.wrap(alloc(), s.getBytes()));
    }

    /**
     * @see #end(ByteBuf)
     */
    default HttpServerResponse end(String s, Hooks<ChannelFuture> h) {
        h.call(end(Netty.wrap(alloc(), s.getBytes())));
        return this;
    }

    /**
     * @see #json(Object)
     */
    default void json(final Object obj, Hooks<ChannelFuture> h) {
        h.call(json(obj));
    }

    /**
     * @see #html(String)
     * @since 0.07
     */
    default void html(String html, Hooks<ChannelFuture> h) {
        h.call(html(html));
    }

    default Outbound write(ByteBuf buf) {
        return send(buf, false);
    }

    default Outbound write(byte[] bytes) {
        return write(Netty.wrap(alloc(), bytes));
    }

    default HttpServerResponse write(ByteBuf buf, Hooks<ChannelFuture> h) {
        h.call(write(buf).then());
        return this;
    }

    default HttpServerResponse write(byte[] bytes, Hooks<ChannelFuture> h) {
        h.call(write(bytes).then());
        return this;
    }

    default Outbound write(String s, Charset charset) {
        return write(s.getBytes(charset));
    }

    default HttpServerResponse write(String s, Charset charset, Hooks<ChannelFuture> h) {
        h.call(write(s, charset).then());
        return this;
    }

    default Outbound write(String s) {
        return write(s, Lang.CHARSET);
    }

    default HttpServerResponse write(String s, Hooks<ChannelFuture> h) {
        h.call(write(s).then());
        return this;
    }

    default ChannelFuture writeFlush(String s) {
        return sendAndFlush(s.getBytes(Lang.CHARSET)).then();
    }

    default ChannelFuture writeFlush(byte[] bytes) {
        return sendAndFlush(bytes).then();
    }

}