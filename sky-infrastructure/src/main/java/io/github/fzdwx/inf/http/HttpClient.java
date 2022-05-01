package io.github.fzdwx.inf.http;

import io.github.fzdwx.inf.core.ClientInf;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;

/**
 * todo
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/25 11:19
 */
public class HttpClient extends ClientInf<HttpClient> {

    public HttpClient(final int port) {
        super(port);
    }

    @Override
    public void addClientOptions(final Bootstrap bootstrap) {

    }

    @Override
    public Hooks<SocketChannel> mountInitChannel() {
        return null;
    }

    @Override
    protected HttpClient me() {
        return this;
    }
}