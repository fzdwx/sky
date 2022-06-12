package core.http.response;

import io.github.fzdwx.lambada.Assert;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/6/12 14:09
 */
public class HttpResponse<Body> {

    /**
     * http status code
     *
     * @see io.netty.handler.codec.http.HttpResponseStatus
     */
    protected HttpResponseStatus status;

    /**
     * @see Type
     */
    protected int type;

    protected Body body;

    HttpResponse(final int type, final HttpResponseStatus status) {
        this.status = status;
        this.type = type;
    }

    public static <Body> HttpResponse<Body> ok() {
        return new HttpResponse<>(Type.TO_STRING, HttpResponseStatus.OK);
    }

    public static <Body> HttpResponse<Body> of(HttpResponseStatus status) {
        return new HttpResponse<>(Type.TO_STRING, status);
    }

    public static <Body> HttpResponse<Body> ok(Runnable r) {
        final HttpResponse<Body> resp = new HttpResponse<>(Type.TO_STRING, HttpResponseStatus.OK);
        resp.body(r);
        return resp;
    }

    public static <Body> HttpResponse<Body> ok(Supplier<Body> s) {
        final HttpResponse<Body> resp = new HttpResponse<>(Type.TO_STRING, HttpResponseStatus.OK);
        resp.body(s);
        return resp;
    }

    public static JsonObjectHttpResponse json() {
        return new JsonObjectHttpResponse();
    }

    public static JsonObjectHttpResponse json(Runnable r) {
        final JsonObjectHttpResponse resp = new JsonObjectHttpResponse();
        resp.body(r);
        return resp;
    }

    public static <Body> JsonObjectHttpResponse json(Supplier<Body> s) {
        final JsonObjectHttpResponse resp = new JsonObjectHttpResponse();
        resp.body(s.get());
        return resp;
    }

    public static JsonObjectHttpResponse json(Object body) {
        return new JsonObjectHttpResponse(body);
    }

    public static JsonMapHttpResponse json(String k, Object v) {
        return new JsonMapHttpResponse().put(k, v);
    }

    /**
     * set http response body.
     *
     * @apiNote default is  {{@link Body#toString()} }
     */
    public HttpResponse<Body> body(Body body) {
        this.body = body;
        return this;
    }

    public HttpResponse<Body> body(Runnable r) {
        r.run();
        return this;
    }

    public HttpResponse<Body> body(Supplier<Body> s) {
        this.body = s.get();
        return this;
    }

    /**
     * get http response body.
     */
    public Body body() {
        return body;
    }

    /**
     * set http response status.
     */
    public HttpResponse<Body> status(final HttpResponseStatus status) {
        Assert.nonNull(status);
        return status(status);
    }

    /**
     * set http resposne status.
     */
    public HttpResponse<Body> status(final int code) {
        return status(HttpResponseStatus.valueOf(code));
    }

    /**
     * get http response status.
     */
    public HttpResponseStatus status() {
        return this.status;
    }

    /**
     * @see Type
     */
    public int type() {
        return this.type;
    }

    public interface Type {

        int TO_STRING = 1;

        int JSON = 2;
    }
}