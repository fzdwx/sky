package org.atomicode.inf.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;
import org.atomicode.fzdwx.lambada.fun.Hooks;
import org.atomicode.inf.core.ClientInf;

/**
 * todo
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
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