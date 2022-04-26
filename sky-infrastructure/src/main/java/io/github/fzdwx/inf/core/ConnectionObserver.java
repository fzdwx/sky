package io.github.fzdwx.inf.core;

import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.lambada.lang.NvMap;

@FunctionalInterface
public interface ConnectionObserver {

    /**
     * Return a noop connection listener
     *
     * @return a noop connection listener
     */
    static ConnectionObserver emptyListener() {
        return Netty.NOOP_LISTENER;
    }

    /**
     * Connection listener {@link NvMap}
     *
     * @return current {@link NvMap} or {@link NvMap#create()}
     */
    default NvMap currentContext() {
        return NvMap.create();
    }

    /**
     * React on connection fatal error, will request a disconnecting state
     * change by default. It should only catch exceptions that can't be consumed by a
     * {@link NettyInbound#receive} subscriber.
     *
     * @param connection the remote connection
     * @param error      the failing cause
     */
    default void onUncaughtException(Connection connection, Throwable error) {
        onStateChange(connection, State.DISCONNECTING);
    }

    /**
     * React on connection state change (e.g. http request or response)
     *
     * @param connection the connection reference
     * @param newState   the new State
     */
    void onStateChange(Connection connection, State newState);

    /**
     * Chain together another {@link ConnectionObserver}
     *
     * @param other the next {@link ConnectionObserver}
     * @return a new composite {@link ConnectionObserver}
     */
    default ConnectionObserver then(ConnectionObserver other) {
        return Netty.compositeConnectionObserver(this, other);
    }

    /**
     * A marker interface for various state signals used in {@link #onStateChange(Connection, State)}
     * <p>
     * Specific protocol might implement more state type for instance
     * request/response lifecycle.
     */
    //CHECKSTYLE:OFF
    interface State {

        /**
         * Propagated when a connection has been established and is available
         */
        State CONNECTED = Netty.CONNECTED;

        /**
         * Propagated when a connection is bound to a channelOperation and ready for
         * user interaction
         */
        State CONFIGURED = Netty.CONFIGURED;

        /**
         * Propagated when a connection has been reused / acquired
         * (keep-alive or pooling)
         */
        State ACQUIRED = Netty.ACQUIRED;

        /**
         * Propagated when a connection has been released but not fully closed
         * (keep-alive or pooling)
         */
        State RELEASED = Netty.RELEASED;

        /**
         * Propagated when a connection is being fully closed
         */
        State DISCONNECTING = Netty.DISCONNECTING;
    }
    //CHECKSTYLE:ON
}