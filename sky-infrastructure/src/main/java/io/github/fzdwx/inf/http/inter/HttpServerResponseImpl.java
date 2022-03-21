package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.inf.ser.Json;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.ArrayList;
import java.util.List;

import static io.github.fzdwx.inf.Netty.close;
import static io.github.fzdwx.inf.http.core.ContentType.JSON;
import static io.github.fzdwx.inf.http.core.ContentType.TEXT_HTML;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:27
 */
public class HttpServerResponseImpl implements HttpServerResponse {

    private static final String RESPONSE_WRITTEN = "Response has already been written";

    private final Channel channel;
    private final HttpServerRequest request;
    private final HttpResponseStatus status;
    private final HttpHeaders headers;
    private final HttpHeaders trailingHeaders = EmptyHttpHeaders.INSTANCE;
    private final HttpVersion version;
    private final boolean keepAlive;
    private final boolean head; // method type is head?

    private List<Cookie> cookie;
    private String contentType;
    private String contentDisposition;

    private Hooks<Void> bodyEndHooks;

    /**
     * 标识是否已经写入了头响应
     */
    private boolean headWritten;
    /**
     * 标记是否已经写入结束了
     */
    private boolean endWritten;
    /**
     * 标识已经写入了多少字节
     */
    private long bytesWritten;
    private boolean closed;

    public HttpServerResponseImpl(final Channel channel, final HttpServerRequest httpRequest) {
        this.channel = channel;
        this.request = httpRequest;
        this.headers = new DefaultHttpHeaders();
        this.version = httpRequest.version();
        this.status = HttpResponseStatus.OK;
        this.keepAlive = (version == HttpVersion.HTTP_1_1 && !request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, true))
                || (version == HttpVersion.HTTP_1_0 && request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true));
        this.head = request.methodType() == RequestMethod.HEAD;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpVersion version() {
        return this.version;
    }

    @Override
    public HttpServerResponse registerBodyEnd(final Hooks<Void> endH) {
        this.bodyEndHooks = endH;
        return this;
    }

    @Override
    public HttpServerResponse chunked() {
        if (version != HttpVersion.HTTP_1_0) {
            headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        }
        return this;
    }

    @Override
    public HttpServerResponse unChunked() {
        if (version != HttpVersion.HTTP_1_0) {
            headers.remove(HttpHeaderNames.TRANSFER_ENCODING);
        }
        return this;
    }

    @Override
    public boolean isChunked() {
        return headers.contains(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true);
    }

    @Override
    public HttpServerResponse header(final CharSequence key, final CharSequence val) {
        headers.set(key, val);
        return this;
    }

    @Override
    public HttpServerResponse cookie(final String key, final String val) {
        if (this.cookie == null) this.cookie = new ArrayList<>();

        this.cookie.add(new DefaultCookie(key, val));
        return this;
    }

    @Override
    public ChannelFuture write(final ByteBuf buf) {
        final var promise = channel.newPromise();

        write(buf, promise);

        return promise;
    }

    @Override
    public ChannelFuture end(final ByteBuf buf) {
        final var promise = channel.newPromise();
        end(buf, promise);
        return promise;
    }

    @Override
    public void close() {
        if (!closed) {
            if (headWritten) {
                channel.writeAndFlush(Netty.empty).addListener(close);
            } else {
                channel.close();
            }
            closed = true;
        }
    }

    @Override
    public ChannelFuture reject() {
        return channel.close();
    }

    @Override
    public ChannelFuture reject(final HttpMessage result) {
        return channel.writeAndFlush(result).addListener(close);
    }

    @Override
    public void redirect(final String url) {
        redirect(url, c -> c.addListener(f -> channel.flush().close()));
    }

    @Override
    public void redirect(final String url, final Hooks<ChannelFuture> h) {
        h.call(channel.write(HttpResult.redirect(url)));
    }

    @Override
    public ChannelFuture json(final Object obj) {
        if (obj == null) {
            this.header(HttpHeaderNames.CONTENT_TYPE, JSON);
            return end("null");
        }

        // no catch exception
        final var buf = Json.codec.encodeToBuf(obj);
        this.header(HttpHeaderNames.CONTENT_TYPE, JSON);
        return end(buf);
    }

    @Override
    public ChannelFuture html(final String html) {
        this.header(HttpHeaderNames.CONTENT_TYPE, TEXT_HTML);
        return end(html);
    }

    @Override
    public void end(final byte[] bytes) {
        // TODO: 2022/3/21  
    }

    @Override
    public HttpServerResponse contentType(final String contentType) {
        this.headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public HttpServerResponse contentDisposition(String fileName) {
        if (fileName == null) return this;

        this.contentDisposition = "attachment; filename=" + fileName;
        this.headers.set(HttpHeaderNames.CONTENT_DISPOSITION, contentDisposition);
        return this;
    }

    @Override
    public HttpServerResponse contentDispositionFull(String contentDisposition) {
        if (contentDisposition == null) return this;

        this.contentDisposition = contentDisposition;
        return this;
    }

    private void end(final ByteBuf buf, final ChannelPromise promise) {
        if (endWritten) { // 已经调用过end了
            throw new IllegalStateException(RESPONSE_WRITTEN);
        }

        bytesWritten += buf.readableBytes();
        HttpObject msg;
        if (!headWritten) { // 如果 response head 没有写入,则一起写
            prepareHeaders(bytesWritten);
            msg = new AssembledFullHttpResponse(head, version, status, headers, buf, trailingHeaders);
        } else {
            msg = new AssembledLastHttpContent(buf, trailingHeaders);
        }

        endWritten = true;
        if (bodyEndHooks != null) {
            bodyEndHooks.call(null);
        }

        if (!keepAlive) {
            this.closed = true;
            this.channel.writeAndFlush(msg, promise).addListener(close);
        } else {
            this.channel.write(msg, promise);
        }
    }

    private HttpServerResponse write(ByteBuf buf, ChannelPromise promise) {
        // preCheck
        if (!headWritten && !headers.contains(HttpHeaderNames.TRANSFER_ENCODING) && !headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
            if (version != HttpVersion.HTTP_1_0) {
                throw new IllegalStateException("You must set the Content-Length header to be the total size of the message "
                        + "body BEFORE sending any data if you are not using HTTP chunked encoding.");
            }
        }

        bytesWritten += buf.readableBytes();
        HttpObject response;
        if (!headWritten) { // don't have written head response(e.g. contentType,http status,content length...)
            prepareHeaders(-1);
            response = new AssembledHttpResponse(head, version, status, headers, buf);
        } else {
            response = new DefaultHttpContent(buf);
        }

        // written response to client
        channel.write(response, promise);

        return this;
    }

    private void prepareHeaders(final long contentLength) {
        if (version == HttpVersion.HTTP_1_0 && keepAlive) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderNames.KEEP_ALIVE);
        } else if (version == HttpVersion.HTTP_1_1 && !keepAlive) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }
        if (head || status == HttpResponseStatus.NOT_MODIFIED) {
            // For HEAD request or NOT_MODIFIED response
            // don't set automatically the content-length
            // and remove the transfer-encoding
            headers.remove(HttpHeaderNames.TRANSFER_ENCODING);
        } else {
            // Set content-length header automatically
            if (contentLength >= 0 && !headers.contains(HttpHeaderNames.CONTENT_LENGTH) && !headers.contains(HttpHeaderNames.TRANSFER_ENCODING)) {
                String value = contentLength == 0 ? "0" : String.valueOf(contentLength);
                headers.set(HttpHeaderNames.CONTENT_LENGTH, value);
            }
        }

        if (cookie != null) {
            setCookies();
        }

        headWritten = true;
    }

    private void setCookies() {
        for (final Cookie c : cookie) {
            headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(c));
        }
    }
}