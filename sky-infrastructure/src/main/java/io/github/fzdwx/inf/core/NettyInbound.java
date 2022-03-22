package io.github.fzdwx.inf.core;

import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/22 22:24
 */
public interface NettyInbound {

    /**
     * receive from connection.
     */
    ByteBuf receive();

    /**
     * receive from connection.
     */
    Object receiveObject();

    /**
     * operation underlying connection.
     */
    NettyInbound withConnection(Consumer<? super Connection> connection);

    static NettyInbound unavailableInbound(Connection connection) {
        return new NettyInbound() {

            private Connection c = connection;

            @Override
            public ByteBuf receive() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object receiveObject() {
                throw new UnsupportedOperationException();
            }

            @Override
            public NettyInbound withConnection(final Consumer<? super Connection> connection) {
                connection.accept(c);
                return this;
            }
        };
    }
}