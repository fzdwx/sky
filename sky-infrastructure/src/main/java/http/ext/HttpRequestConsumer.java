package http.ext;

import http.HttpServerRequest;
import http.HttpServerResponse;

import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 16:39
 */
@FunctionalInterface
public interface HttpRequestConsumer {

    void consume(HttpServerRequest req, HttpServerResponse response) throws InvocationTargetException, IllegalAccessException;
}