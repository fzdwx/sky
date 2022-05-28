package core.group;

import io.netty.channel.ChannelException;
import io.netty.util.internal.ObjectUtil;
import core.socket.Socket;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * copy from netty
 */
public class SocketGroupException extends ChannelException implements Iterable<Map.Entry<Socket, Throwable>> {

    private static final long serialVersionUID = -4093064295562629453L;
    private final Collection<Map.Entry<Socket, Throwable>> failed;

    public SocketGroupException(Collection<Map.Entry<Socket, Throwable>> causes) {
        ObjectUtil.checkNonEmpty(causes, "causes");

        failed = Collections.unmodifiableCollection(causes);
    }

    /**
     * Returns a {@link Iterator} which contains all the {@link Throwable} that was a cause of the failure and the
     * related id of the {@link Socket}.
     */
    @Override
    public Iterator<Map.Entry<Socket, Throwable>> iterator() {
        return failed.iterator();
    }
}