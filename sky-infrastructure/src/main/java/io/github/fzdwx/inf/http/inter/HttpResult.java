package io.github.fzdwx.inf.http.inter;

import cn.hutool.core.io.FileUtil;
import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.ContentType;
import io.github.fzdwx.lambada.Lang;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http result.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @apiNote 一个完整的http响应
 * @date 2022/03/18 14:25:07
 */
public class HttpResult extends DefaultFullHttpResponse {

    private static final PooledByteBufAllocator BYTE_BUF_ALLOCATOR = new PooledByteBufAllocator(false);

    private static final byte[] CONTENT_NORMAL_200 = "{\"code\":200,\"message\":\"OK\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CONTENT_ERROR_401 = "{\"code\":401,\"message\":\"UNAUTHORIZED\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CONTENT_ERROR_404 = "{\"code\":404,\"message\":\"REQUEST PATH NOT FOUND\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CONTENT_ERROR_405 = "{\"code\":405,\"message\":\"METHOD NOT ALLOWED\"}".getBytes(StandardCharsets.UTF_8);
    private static final String CONTENT_ERROR_500 = "{\"code\":500,\"message\":\"%s\"}";
    public static HttpResult NOT_FOUND = new HttpResult(HttpResponseStatus.NOT_FOUND, Netty.alloc(CONTENT_ERROR_404));
    private byte[] body;

    private HttpResult(final HttpResponseStatus status, final ByteBuf buffer) {
        super(HTTP_1_1, status, buffer);
        this.headers().set(CONTENT_TYPE, ContentType.TEXT_HTML);
        this.headers().setInt(CONTENT_LENGTH, this.content().readableBytes());
        /*
         * 支持CORS 跨域访问
         */
        this.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        this.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept, RCS-ACCESS-TOKEN");
        this.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");
    }

    private HttpResult(final HttpResponseStatus status) {
        super(HTTP_1_1, status, null);
    }

    public static HttpResult make(final HttpResponseStatus status) {
        if (HttpResponseStatus.UNAUTHORIZED == status) {
            return make(HttpResponseStatus.UNAUTHORIZED, CONTENT_ERROR_401);
        }
        if (HttpResponseStatus.NOT_FOUND == status) {
            return make(HttpResponseStatus.NOT_FOUND, CONTENT_ERROR_404);
        }
        if (HttpResponseStatus.METHOD_NOT_ALLOWED == status) {
            return make(HttpResponseStatus.METHOD_NOT_ALLOWED, CONTENT_ERROR_405);
        }
        return make(OK, CONTENT_NORMAL_200);
    }

    public static HttpResult fail(final Exception exception) {
        final String message = exception.getClass().getName() + ":" + exception.getMessage();
        return make(HttpResponseStatus.INTERNAL_SERVER_ERROR, String.format(CONTENT_ERROR_500, message).getBytes(StandardCharsets.UTF_8));
    }

    public static HttpResult fail(final String errorMessage) {
        return make(HttpResponseStatus.INTERNAL_SERVER_ERROR, String.format(CONTENT_ERROR_500, errorMessage).getBytes(StandardCharsets.UTF_8));
    }

    public static HttpResult ok(final String content) {
        return make(OK, content.getBytes());
    }

    public static HttpResult ok(final byte[] body) {
        return make(OK, body);
    }

    public static HttpResult ok() {
        return ok(HttpResult.CONTENT_NORMAL_200);
    }

    /**
     * set CONTENT_TYPE with application/json
     */
    public HttpResult json() {
        this.headers().set(CONTENT_TYPE, ContentType.JSON);
        return this;
    }

    /**
     * set CONTENT_TYPE to text/plain
     */
    public HttpResult text() {
        this.headers().set(CONTENT_TYPE, ContentType.TEXT_PLAIN);
        return this;
    }

    /**
     * set CONTENT_TYPE to application/octet-stream
     */
    public HttpResult octetStream() {
        this.headers().set(CONTENT_TYPE, ContentType.OCTET_STREAM);
        return this;
    }

    public HttpResult contentType(final String contentType) {
        if (contentType == null) return this;

        this.headers().set(CONTENT_TYPE, contentType);
        return this;
    }

    public HttpResult contentLength(final long length) {
        this.headers().set(CONTENT_LENGTH, length);
        return this;
    }

    public HttpResult contentDisposition(String fileName) {
        if (fileName == null) return this;

        headers().add(CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return this;
    }

    public HttpResult contentDispositionFull(String contentDisposition) {
        if (contentDisposition == null) return this;

        headers().add(CONTENT_DISPOSITION, contentDisposition);
        return this;
    }

    @Override
    public String toString() {
        return this.protocolVersion().toString() + " " + this.status().toString() + "\n" +
               CONTENT_TYPE + ": " + this.headers().get(CONTENT_TYPE) + "\n" +
               CONTENT_LENGTH + ": " + this.headers().get(CONTENT_LENGTH) + "\n" +
               "content-body" + ": " + new String(this.body) + "\n";
    }

    public static HttpResult redirect(final String url) {
        final var httpResult = new HttpResult(HttpResponseStatus.MOVED_PERMANENTLY, Unpooled.copiedBuffer(url, StandardCharsets.UTF_8));
        httpResult.headers().add(HttpHeaderNames.LOCATION, url);
        return httpResult;
    }

    public static HttpResult file(final File file) {
        final var httpResult = ok(FileUtil.readBytes(file));
        httpResult.headers().add(CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        return httpResult;
    }

    public static HttpResult file(final byte[] bytes, final String fileName) {
        if (Lang.isEmpty(fileName)) {
            throw new IllegalArgumentException("fileName can not be empty");
        }

        return ok(bytes).contentDisposition(fileName);
    }

    private static HttpResult make(final HttpResponseStatus status, final byte[] body) {
        final ByteBuf buffer = BYTE_BUF_ALLOCATOR.buffer(body.length);
        buffer.writeBytes(body);
        return new HttpResult(status, buffer);
    }
}