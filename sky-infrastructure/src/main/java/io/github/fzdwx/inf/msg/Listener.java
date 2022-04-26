package io.github.fzdwx.inf.msg;

import io.github.fzdwx.inf.route.msg.SocketSession;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 12:26
 * @since 0.06
 */
@FunctionalInterface
public interface Listener {

    /**
     * do before handshake
     *
     * @param session socket session
     * @throws RuntimeException when verification failed you kan choose throw exception
     */
    default void beforeHandshake(SocketSession session) throws RuntimeException {

    }

    /**
     * on client connect server success.
     *
     * @param session socket session
     */
    default void onOpen(final SocketSession session) {

    }

    /**
     * on client close connect
     *
     * @param session socket session
     */
    default void onclose(SocketSession session) {

    }

    /**
     * Gets called if an user event was triggered.
     *
     * @param session socket session
     * @param event   event
     */
    default void onEvent(SocketSession session, Object event) {

    }

    void onText(SocketSession session, final String text);

    default void onBinary(SocketSession session, ByteBuf content) {

    }

    /**
     * Gets called if a {@link Throwable} was thrown.
     *
     * @param session socket session
     * @param cause   exc
     */
    default void onError(SocketSession session, Throwable cause) {

    }
}