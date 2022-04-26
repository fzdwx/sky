package org.atomicode.inf.core.exception;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/23 14:41
 */
public class ChannelException extends RuntimeException {

    static final String CONNECTION_CLOSED_BEFORE_SEND = "Connection has been closed BEFORE send operation";

    public ChannelException(final String message) {
        super(message);
    }

    public static ChannelException beforeSend() {
        return new ChannelException(CONNECTION_CLOSED_BEFORE_SEND);
    }
}