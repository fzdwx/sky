package core.socket;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
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
    default void beforeHandshake(Socket session) throws RuntimeException {

    }

    /**
     * on client connect server success.
     *
     * @param session socket session
     */
    default void onOpen(final Socket session) {

    }

    /**
     * on client close connect
     *
     * @param session socket session
     */
    default void onclose(Socket session) {

    }

    /**
     * Gets called if an user event was triggered.
     *
     * @param session socket session
     * @param event   event
     */
    default void onEvent(Socket session, Object event) {

    }

    void onText(Socket session, final String text);

    default void onBinary(Socket session, byte[] content) {

    }

    default void onPing(byte[] ping) {

    }

    default void onPong(byte[] pong) {

    }

    /**
     * Gets called if a {@link Throwable} was thrown.
     *
     * @param session socket session
     * @param cause   exc
     */
    default void onError(Socket session, Throwable cause) {

    }
}