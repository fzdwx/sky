package http;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/7 20:03
 */
@FunctionalInterface
public interface HttpExceptionHandler {

    void handler(Exception e, HttpServerResponse resp);
}