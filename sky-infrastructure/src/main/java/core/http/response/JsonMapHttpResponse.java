package core.http.response;

import io.github.fzdwx.lambada.Collections;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/6/12 14:26
 */
public class JsonMapHttpResponse extends HttpResponse<Map<String, Object>> {

    JsonMapHttpResponse() {
        super(Type.JSON, HttpResponseStatus.OK);
    }

    public JsonMapHttpResponse put(final String k, final Object v) {
        if (this.body == null) this.body = Collections.map();

        this.body.put(k, v);
        return this;
    }
}