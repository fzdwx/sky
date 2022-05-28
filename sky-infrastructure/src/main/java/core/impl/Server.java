package core.impl;

import core.AbstractServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * server.
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/5/6 14:35
 */
@Slf4j
public class Server extends AbstractServer<Server> {

    private final Map<ChannelOption<?>, Object> serverOptions = new HashMap<>();
    private final Map<ChannelOption<?>, Object> childOptions = new HashMap<>();
    private final List<ChannelHandler> serverHandlers = new ArrayList<>();

    public Server() {
        super();
    }

    @Override
    public Server impl() {
        return this;
    }

    @Override
    protected void preListen(final InetSocketAddress address) {
        super.preListen(address);

        if (serverHandlers.size() > 0) {
            serverHandlers.forEach(serverBootstrap::handler);
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : serverOptions.entrySet()) {
            this.serverBootstrap = serverBootstrap.option((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChannelOption<?>, ?> entry : childOptions.entrySet()) {
            this.serverBootstrap = serverBootstrap.childOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
        }
    }

    public <T> Server withServerOptions(ChannelOption<T> option, T t) {
        checkStart();
        serverOptions.put(option, t);
        return this;
    }

    public Server withServerHandler(ChannelHandler handler) {
        checkStart();
        serverHandlers.add(handler);
        return this;
    }

    public <T> Server withChildOptions(ChannelOption<T> option, T t) {
        checkStart();
        childOptions.put(option, t);

        return this;
    }

}