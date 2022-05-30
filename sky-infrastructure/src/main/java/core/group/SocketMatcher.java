package core.group;

import core.socket.Socket;

public interface SocketMatcher {

    static final SocketMatcher ALL = socket -> true;

    /**
     * Returns {@code true} if the operation should be also executed on the given {@link Socket}.
     */
    boolean matches(Socket socket);

}