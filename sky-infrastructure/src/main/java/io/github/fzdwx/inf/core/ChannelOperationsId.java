package io.github.fzdwx.inf.core;

public interface ChannelOperationsId {

    /**
     * The short string is a combination of the id of the underlying connection
     * and in case of HTTP, the serial number of the request received on that connection.
     * <p>Format of the short string:
     * <pre>
     * {@code <CONNECTION_ID>-<REQUEST_NUMBER>}
     * </pre>
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     *     <CONNECTION_ID>: 329c6ffd
     *     <REQUEST_NUMBER>: 5
     *
     *     Result: 329c6ffd-5
     * }
     * </pre>
     * </p>
     */
    String asShortText();

    /**
     * The long string is a combination of the id of the underlying connection, local and remote addresses,
     * and in case of HTTP, the serial number of the request received on that connection.
     * <p>Format of the long string:
     * <pre>
     * {@code <CONNECTION_ID>-<REQUEST_NUMBER>, L:<LOCAL_ADDRESS> <CONNECTION_OPENED_CLOSED> R:<REMOTE_ADDRESS>}
     * </pre>
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     * Opened connection
     *     <CONNECTION_ID>: 329c6ffd
     *     <REQUEST_NUMBER>: 5
     *     <LOCAL_ADDRESS>: /0:0:0:0:0:0:0:1:64286
     *     <CONNECTION_OPENED_CLOSED>: - (opened)
     *     <REMOTE_ADDRESS>: /0:0:0:0:0:0:0:1:64284
     *
     *     Result: 329c6ffd-5, L:/0:0:0:0:0:0:0:1:64286 - R:/0:0:0:0:0:0:0:1:64284
     *
     * Closed connection
     *     <CONNECTION_ID>: 329c6ffd
     *     <REQUEST_NUMBER>: 5
     *     <LOCAL_ADDRESS>: /0:0:0:0:0:0:0:1:64286
     *     <CONNECTION_OPENED_CLOSED>: ! (closed)
     *     <REMOTE_ADDRESS>: /0:0:0:0:0:0:0:1:64284
     *
     *     Result: 329c6ffd-5, L:/0:0:0:0:0:0:0:1:64286 ! R:/0:0:0:0:0:0:0:1:64284
     * }
     * </pre>
     * </p>
     */
    String asLongText();
}