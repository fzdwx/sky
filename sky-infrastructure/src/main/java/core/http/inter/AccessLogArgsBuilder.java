package core.http.inter;

import io.netty.handler.codec.http.HttpRequest;

import java.net.SocketAddress;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/6/1 14:34
 */
public class AccessLogArgsBuilder {

    private final SocketAddress remoteAddress;

    public AccessLogArgsBuilder(final SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void mountRequest(final HttpRequest req) {

    }
}