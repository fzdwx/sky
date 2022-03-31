package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.core.ChannelOutBound;
import io.github.fzdwx.inf.core.NettyOutbound;
import io.github.fzdwx.inf.core.exception.ChannelException;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import io.github.fzdwx.inf.route.inter.RequestMethod;
import io.github.fzdwx.inf.ser.Json;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static io.github.fzdwx.inf.Netty.close;
import static io.github.fzdwx.inf.Netty.empty;
import static io.github.fzdwx.inf.http.core.ContentType.JSON;
import static io.github.fzdwx.inf.http.core.ContentType.TEXT_HTML;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:27
 */
@Slf4j
public class HttpServerResponseImpl extends ChannelOutBound implements HttpServerResponse {

    final static AtomicIntegerFieldUpdater<HttpServerResponseImpl> HEAD_STATE =
            AtomicIntegerFieldUpdater.newUpdater(HttpServerResponseImpl.class,
                    "headWritten");
    final static AtomicIntegerFieldUpdater<HttpServerResponseImpl> END_STATE =
            AtomicIntegerFieldUpdater.newUpdater(HttpServerResponseImpl.class,
                    "endWritten");
    private static final String RESPONSE_WRITTEN = "Response has already been written";
    private static final String HEAD_NOT_WRITTEN = "Head response has not been written";
    private static final String HEAD_ALREADY_WRITTEN = "Head already written";
    private final static ChannelInboundHandler HTTP_EXTRACTOR = Netty.inboundHandler(
            (ctx, msg) -> {
                if (msg instanceof ByteBufHolder) {
                    if (msg instanceof FullHttpMessage) {
                        // TODO convert into 2 messages if FullHttpMessage
                        ctx.fireChannelRead(msg);
                    } else {
                        ByteBuf bb = ((ByteBufHolder) msg).content();
                        ctx.fireChannelRead(bb);
                        if (msg instanceof LastHttpContent) {
                            ctx.fireChannelRead(LastHttpContent.EMPTY_LAST_CONTENT);
                        }
                    }
                } else {
                    ctx.fireChannelRead(msg);
                }
            }
    );
    private final Channel channel;
    private final HttpServerRequest request;
    private final HttpHeaders headers;
    private final HttpHeaders trailingHeaders = EmptyHttpHeaders.INSTANCE;
    private final HttpVersion version;
    private final boolean keepAlive;
    private final boolean head; // method type is head?
    private HttpResponseStatus status;
    private List<Cookie> cookie;
    private String contentDisposition;
    private Hooks<Void> bodyEndHooks;
    /**
     * 标识是否已经写入了头响应
     */
    private volatile int headWritten;
    /**
     * 标记是否已经写入结束了
     */
    private volatile int endWritten;
    /**
     * 标识已经写入了多少字节
     */
    private long bytesWritten;
    private boolean closed;

    public HttpServerResponseImpl(final Channel channel, final HttpServerRequest httpRequest) {
        super(channel);
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
    public NettyOutbound send(final ByteBuf data, final boolean flush) {
        if (!channel().isActive()) {
            return then(ChannelException.beforeSend());
        }

        // preCheck
        if (!headWritten() && !headers.contains(HttpHeaderNames.TRANSFER_ENCODING) && !headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
            if (version != HttpVersion.HTTP_1_0) {
                then(new IllegalStateException("You must set the Content-Length header to be the total size of the message "
                        + "body BEFORE sending any data if you are not using HTTP chunked encoding."));
            }
        }

        return super.send(data, flush);
    }

    @Override
    public NettyOutbound sendChunk(final InputStream in, final int chunkSize) {
        chunked();
        return send(empty, true)
                .then(h -> super.sendChunk(in, chunkSize))
                .then(end(empty));
    }

    @Override
    public Object wrapData(final ByteBuf data) {

        bytesWritten += data.readableBytes();
        HttpObject response;
        if (!headWritten()) { // don't have written head response(e.g. contentType,http status,content length...)
            prepareHeaders(bytesWritten);
            response = new AssembledHttpResponse(head, version, status, headers, data);
        } else {
            response = new DefaultHttpContent(data);
        }

        return response;
    }

    @Override
    public ChunkedInput<?> wrapChunkData(final InputStream in, final int chunkSize) {
        if (in instanceof ReadableByteChannel ins) {
            return new HttpChunkedInput(new ChunkedNioStream(ins, chunkSize));
        }
        return new HttpChunkedInput(new ChunkedStream(in, chunkSize));
    }

    @Override
    protected void onOutboundComplete(final Future<? super Void> f) {
        end(empty);
    }

    @Override
    public NettyOutbound sendFile(final Path file) {
        try {
            return sendFile(file, 0, Files.size(file));
        } catch (IOException e) {
            return then(sendNotFound());
        }
    }

    @Override
    public ChannelFuture then() {
        if (!channel.isActive()) {
            return channel.newFailedFuture(ChannelException.beforeSend());
        }

        final ChannelFuture ff;
        if (!headWritten()) {
            prepareHeaders(-1);
            ff = channel.writeAndFlush(new AssembledHttpResponse(head, version, status, headers, empty));
        } else if (!endWritten()) {
            System.out.println("write end");
            ff = end(empty);
        } else return channel.newSucceededFuture();

        return ff;
    }

    @Override
    public NettyOutbound sendFile(final Path file, final long position, final long count) {
        Objects.requireNonNull(file, "filepath");

        if (endWritten()) { // 已经调用过end了
            return then(new IllegalStateException(RESPONSE_WRITTEN));
        }
        if (headWritten()) {
            return then(new IllegalStateException(HEAD_ALREADY_WRITTEN));
        }

        if (!Netty.isTransferEncodingChunked(headers) && !Netty.isContentLengthSet(
                headers) && count < Integer.MAX_VALUE) {
            headers.setInt(HttpHeaderNames.CONTENT_LENGTH, (int) count);
        } else if (!Netty.isContentLengthSet(headers)) {
            headers
                    .remove(HttpHeaderNames.CONTENT_LENGTH)
                    .remove(HttpHeaderNames.TRANSFER_ENCODING);
            Netty.setTransferEncodingChunked(headers, true);
        }

        return super.sendFile(file, position, count);
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public boolean isEnd() {
        return END_STATE.get(this) != 0;
    }

    @Override
    public HttpVersion version() {
        return this.version;
    }

    @Override
    public HttpServerResponse status(final HttpResponseStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public HttpServerResponse keepAlive(final boolean keepAlive) {
        HttpUtil.setKeepAlive(headers, version, keepAlive);
        return this;
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

    @Override
    public HttpServerResponse mountBodyEnd(final Hooks<Void> endH) {
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
        return send(buf, false).then();
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
    public ChannelFuture end(final ByteBuf buf) {
        final var promise = channel.newPromise();
        end(buf, promise);
        return promise;
    }

    @Override
    public void close() {
        if (!closed) {
            if (headWritten()) {
                channel.writeAndFlush(Netty.empty).addListener(close);
            } else {
                channel.close();
            }
            closed = true;
        }
    }

    boolean headWritten() {
        return headWritten > 0;
    }

    boolean endWritten() {
        return endWritten > 0;
    }

    private void end(final ByteBuf buf, final ChannelPromise promise) {
        if (endWritten()) { // 已经调用过end了
            throw new IllegalStateException(RESPONSE_WRITTEN);
        }

        bytesWritten += buf.readableBytes();
        HttpObject msg;
        if (!headWritten()) { // 如果 response head 没有写入,则一起写
            prepareHeaders(bytesWritten);
            msg = new AssembledFullHttpResponse(head, version, status, headers, buf, trailingHeaders);
        } else {
            msg = new AssembledLastHttpContent(buf, trailingHeaders); // mark it is last content
        }

        afterEnd(promise, msg);
    }

    private void afterEnd(final ChannelPromise promise, final Object msg) {
        if (!END_STATE.compareAndSet(this, 0, 1)) {
            return;
        }

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

    private void prepareHeaders(final long contentLength) {
        if (!HEAD_STATE.compareAndSet(this, 0, 1)) {
            throw new IllegalStateException(HEAD_ALREADY_WRITTEN);
        }

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
    }

    private void setCookies() {
        for (final Cookie c : cookie) {
            headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(c));
        }
    }
}