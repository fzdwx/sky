package core.http.response;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/6/12 14:32
 */
public class JsonObjectHttpResponse extends HttpResponse<Object> {

    JsonObjectHttpResponse() {
        super(Type.JSON, HttpResponseStatus.OK);
    }

    public JsonObjectHttpResponse(final Object body) {
        this();
        this.body = body;
    }
}