package http.inter;

import http.ContentType;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * simple http result.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @apiNote Usually used for the first frame returned by a large amount of data
 * @date 2022/3/20 19:31
 */
public class SimpleHttpResult extends DefaultHttpResponse {

    public SimpleHttpResult(final HttpVersion version, final HttpResponseStatus status) {
        super(version, status);
    }

    public static SimpleHttpResult empty() {
        return empty(OK);
    }

    public static SimpleHttpResult empty(HttpResponseStatus status) {
        return new SimpleHttpResult(HttpVersion.HTTP_1_1, status);
    }

    public SimpleHttpResult contentType(final String contentType) {
        if (contentType == null) return this;

        this.headers().set(CONTENT_TYPE, contentType);
        return this;
    }

    public SimpleHttpResult contentLength(final long length) {
        this.headers().set(CONTENT_LENGTH, length);
        return this;
    }

    public SimpleHttpResult contentDisposition(String fileName) {
        if (fileName == null) return this;

        headers().add(CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return this;
    }

    public SimpleHttpResult contentDispositionFull(String contentDisposition) {
        if (contentDisposition == null) return this;

        headers().add(CONTENT_DISPOSITION, contentDisposition);
        return this;
    }

    /**
     * set CONTENT_TYPE with application/json
     */
    public SimpleHttpResult json() {
        this.headers().set(CONTENT_TYPE, ContentType.JSON);
        return this;
    }
}