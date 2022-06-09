package util;

import core.http.Headers;
import core.http.ext.HttpServerRequest;
import core.http.ext.HttpServerResponse;
import io.github.fzdwx.lambada.Collections;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.lang.KvMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AsciiString;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.UnstableApi;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;
import static io.netty.util.internal.StringUtil.COMMA;

/**
 * netty tool.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/16 21:44
 * @since 0.06
 */
@Slf4j
public final class Netty {

    public static final AttributeKey<String> SubProtocolAttrKey = AttributeKey.valueOf("subProtocol");
    public static final int DEFAULT_CHUNK_SIZE = 8192;
    public static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024 * 1024;
    public static final ByteBuf empty = Unpooled.EMPTY_BUFFER;
    public static final AttributeKey<HttpServerRequest> REQUEST_KEY = AttributeKey.valueOf("request");
    public static final AttributeKey<HttpServerResponse> RESPONSE_KEY = AttributeKey.valueOf("response");
    public static final GenericFutureListener<? extends Future<? super Void>> close = ChannelFutureListener.CLOSE;
    public static final Map<Charset, HttpDataFactory> HTTP_DATA_FACTORY_CACHE = Collections.map();
    public static final String localhost = "localhost";
    private static final AsciiString CHARSET_EQUALS = AsciiString.of(HttpHeaderValues.CHARSET + "=");
    private static final AsciiString SEMICOLON = AsciiString.cached(";");
    private static final String COMMA_STRING = String.valueOf(COMMA);

    public static String read(ByteBuf buf) {
        if (buf == null) return Lang.EMPTY_STR;

        return new String(readBytes(buf));
    }

    public static byte[] readBytes(ByteBuf buf) {
        return ByteBufUtil.getBytes(buf);
    }

    public static ByteBuf wrap(final ByteBufAllocator alloc, final byte[] binary) {
        if (binary == null || binary.length <= 0) {
            return empty;
        }
        final ByteBuf buffer = alloc.buffer(binary.length);
        return buffer.writeBytes(binary);
    }

    public static ByteBuf wrap(final ByteBufAllocator alloc, final String data) {
        return wrap(alloc, data.getBytes());
    }

    public static boolean isWebSocket(HttpHeaders headers) {
        return headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }

    public static boolean isTransferEncodingChunked(HttpHeaders headers) {
        return headers.containsValue(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true);
    }

    public static boolean isContentLengthSet(HttpHeaders headers) {
        return headers.contains(HttpHeaderNames.CONTENT_LENGTH);
    }

    public static void setTransferEncodingChunked(Headers headers, boolean chunked) {
        if (chunked) {
            headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            headers.remove(HttpHeaderNames.CONTENT_LENGTH);
        } else {
            List<String> encodings = headers.getAll(HttpHeaderNames.TRANSFER_ENCODING);
            if (encodings.isEmpty()) {
                return;
            }
            List<CharSequence> values = new ArrayList<CharSequence>(encodings);
            values.removeIf(HttpHeaderValues.CHUNKED::contentEqualsIgnoreCase);
            if (values.isEmpty()) {
                headers.remove(HttpHeaderNames.TRANSFER_ENCODING);
            } else {
                headers.set(HttpHeaderNames.TRANSFER_ENCODING, values);
            }
        }
    }

    public static KvMap params(final String uri) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        final Map<String, List<String>> parameters = queryStringDecoder.parameters();

        KvMap params = KvMap.create();
        if (!parameters.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                params.add(entry.getKey(), entry.getValue());
            }
        }
        return params;
    }

    public static String formatHeaders(Headers headers) {
        return headers.entrySet().stream()
                .map(entry -> {
                    List<String> values = entry.getValue();
                    return entry.getKey() + ":" + (values.size() == 1 ?
                            "\"" + values.get(0) + "\"" :
                            values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public static int findPathEndIndex(String uri) {
        int len = uri.length();
        for (int i = 0; i < len; i++) {
            char c = uri.charAt(i);
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return len;
    }

    public static ChannelPromise promise(final Channel channel) {
        return new DefaultChannelPromise(channel);
    }

    public static ChannelFuture future(final Channel channel) {
        return new DefaultChannelPromise(channel);
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    public static void removeHandler(final Channel channel, final String handlerName) {
        if (channel.isActive() && channel.pipeline()
                .context(handlerName) != null) {
            channel.pipeline()
                    .remove(handlerName);
        }
    }

    public static void replaceHandler(Channel channel, String handlerName, ChannelHandler handler) {
        if (channel.isActive() && channel.pipeline()
                .context(handlerName) != null) {
            channel.pipeline()
                    .replace(handlerName, handlerName, handler);

        }
    }

    /**
     * Create a new {@link ChannelInboundHandler} that will invoke
     * {@link BiConsumer#accept} on
     * {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)}.
     *
     * @param handler the channel-read callback
     * @return a marking event used when a netty connector handler terminates
     */
    public static ChannelInboundHandler inboundHandler(BiConsumer<? super ChannelHandlerContext, Object> handler) {
        return new Netty.ExtractorHandler(handler);
    }

    public static boolean isFormUrlEncoder(final String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.contains("application/x-www-form-urlencoded");
    }

    public static String toPrettyHexDump(Object msg) {
        Objects.requireNonNull(msg, "msg");
        String result;
        if (msg instanceof ByteBufHolder &&
                !Objects.equals(Unpooled.EMPTY_BUFFER, ((ByteBufHolder) msg).content())) {
            ByteBuf buffer = ((ByteBufHolder) msg).content();
            result = "\n" + ByteBufUtil.prettyHexDump(buffer);
        } else if (msg instanceof ByteBuf) {
            result = "\n" + ByteBufUtil.prettyHexDump((ByteBuf) msg);
        } else {
            result = msg.toString();
        }
        return result;
    }

    public static HttpDataFactory dataFactory(Charset charset) {
        final HttpDataFactory httpDataFactory = HTTP_DATA_FACTORY_CACHE.get(charset);
        if (httpDataFactory == null) {
            final DefaultHttpDataFactory defaultHttpDataFactory = new DefaultHttpDataFactory(charset);
            HTTP_DATA_FACTORY_CACHE.put(charset, defaultHttpDataFactory);
            return defaultHttpDataFactory;
        }
        return httpDataFactory;
    }

    /**
     * Determine if a uri is in origin-form according to
     * <a href="https://tools.ietf.org/html/rfc7230#section-5.3">rfc7230, 5.3</a>.
     */
    public static boolean isOriginForm(URI uri) {
        return uri.getScheme() == null && uri.getSchemeSpecificPart() == null &&
                uri.getHost() == null && uri.getAuthority() == null;
    }

    /**
     * Determine if a uri is in asterisk-form according to
     * <a href="https://tools.ietf.org/html/rfc7230#section-5.3">rfc7230, 5.3</a>.
     */
    public static boolean isAsteriskForm(URI uri) {
        return "*".equals(uri.getPath()) &&
                uri.getScheme() == null && uri.getSchemeSpecificPart() == null &&
                uri.getHost() == null && uri.getAuthority() == null && uri.getQuery() == null &&
                uri.getFragment() == null;
    }

    /**
     * Returns {@code true} if and only if the connection can remain open and
     * thus 'kept alive'.  This methods respects the value of the.
     * <p>
     * {@code "Connection"} header first and then the return value of
     * {@link HttpVersion#isKeepAliveDefault()}.
     */
    public static boolean isKeepAlive(HttpMessage message) {
        return !message.headers().containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, true) &&
                (message.protocolVersion().isKeepAliveDefault() ||
                        message.headers().containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true));
    }

    /**
     * Sets the value of the {@code "Connection"} header depending on the
     * protocol version of the specified message. This getMethod sets or removes
     * the {@code "Connection"} header depending on what the default keep alive
     * mode of the message's protocol version is, as specified by
     * {@link HttpVersion#isKeepAliveDefault()}.
     * <ul>
     * <li>If the connection is kept alive by default:
     *     <ul>
     *     <li>set to {@code "close"} if {@code keepAlive} is {@code false}.</li>
     *     <li>remove otherwise.</li>
     *     </ul></li>
     * <li>If the connection is closed by default:
     *     <ul>
     *     <li>set to {@code "keep-alive"} if {@code keepAlive} is {@code true}.</li>
     *     <li>remove otherwise.</li>
     *     </ul></li>
     * </ul>
     *
     * @see #setKeepAlive(HttpHeaders, HttpVersion, boolean)
     */
    public static void setKeepAlive(HttpMessage message, boolean keepAlive) {
        setKeepAlive(message.headers(), message.protocolVersion(), keepAlive);
    }

    /**
     * Sets the value of the {@code "Connection"} header depending on the
     * protocol version of the specified message. This getMethod sets or removes
     * the {@code "Connection"} header depending on what the default keep alive
     * mode of the message's protocol version is, as specified by
     * {@link HttpVersion#isKeepAliveDefault()}.
     * <ul>
     * <li>If the connection is kept alive by default:
     *     <ul>
     *     <li>set to {@code "close"} if {@code keepAlive} is {@code false}.</li>
     *     <li>remove otherwise.</li>
     *     </ul></li>
     * <li>If the connection is closed by default:
     *     <ul>
     *     <li>set to {@code "keep-alive"} if {@code keepAlive} is {@code true}.</li>
     *     <li>remove otherwise.</li>
     *     </ul></li>
     * </ul>
     */
    public static void setKeepAlive(HttpHeaders h, HttpVersion httpVersion, boolean keepAlive) {
        if (httpVersion.isKeepAliveDefault()) {
            if (keepAlive) {
                h.remove(HttpHeaderNames.CONNECTION);
            } else {
                h.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            }
        } else {
            if (keepAlive) {
                h.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            } else {
                h.remove(HttpHeaderNames.CONNECTION);
            }
        }
    }

    /**
     * Returns the length of the content. Please note that this value is
     * not retrieved from {@link HttpContent#content()} but from the
     * {@code "Content-Length"} header, and thus they are independent from each
     * other.
     *
     * @return the content length
     * @throws NumberFormatException if the message does not have the {@code "Content-Length"} header
     *                               or its value is not a number
     */
    public static long getContentLength(HttpMessage message) {
        String value = message.headers().get(HttpHeaderNames.CONTENT_LENGTH);
        if (value != null) {
            return Long.parseLong(value);
        }

        // We know the content length if it's a Web Socket message even if
        // Content-Length header is missing.
        long webSocketContentLength = getWebSocketContentLength(message);
        if (webSocketContentLength >= 0) {
            return webSocketContentLength;
        }

        // Otherwise we don't.
        throw new NumberFormatException("header not found: " + HttpHeaderNames.CONTENT_LENGTH);
    }

    /**
     * Returns the length of the content or the specified default value if the message does not have the {@code
     * "Content-Length" header}. Please note that this value is not retrieved from {@link HttpContent#content()} but
     * from the {@code "Content-Length"} header, and thus they are independent from each other.
     *
     * @param message      the message
     * @param defaultValue the default value
     * @return the content length or the specified default value
     * @throws NumberFormatException if the {@code "Content-Length"} header does not parse as a long
     */
    public static long getContentLength(HttpMessage message, long defaultValue) {
        String value = message.headers().get(HttpHeaderNames.CONTENT_LENGTH);
        if (value != null) {
            return Long.parseLong(value);
        }

        // We know the content length if it's a Web Socket message even if
        // Content-Length header is missing.
        long webSocketContentLength = getWebSocketContentLength(message);
        if (webSocketContentLength >= 0) {
            return webSocketContentLength;
        }

        // Otherwise we don't.
        return defaultValue;
    }

    /**
     * Get an {@code int} representation of {@link #getContentLength(HttpMessage, long)}.
     *
     * @return the content length or {@code defaultValue} if this message does
     * not have the {@code "Content-Length"} header.
     * @throws NumberFormatException if the {@code "Content-Length"} header does not parse as an int
     */
    public static int getContentLength(HttpMessage message, int defaultValue) {
        return (int) Math.min(Integer.MAX_VALUE, getContentLength(message, (long) defaultValue));
    }

    /**
     * Sets the {@code "Content-Length"} header.
     */
    public static void setContentLength(HttpMessage message, long length) {
        message.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
    }

    public static boolean isContentLengthSet(HttpMessage m) {
        return m.headers().contains(HttpHeaderNames.CONTENT_LENGTH);
    }

    /**
     * Returns {@code true} if and only if the specified message contains an expect header and the only expectation
     * present is the 100-continue expectation. Note that this method returns {@code false} if the expect header is
     * not valid for the message (e.g., the message is a response, or the version on the message is HTTP/1.0).
     *
     * @param message the message
     * @return {@code true} if and only if the expectation 100-continue is present and it is the only expectation
     * present
     */
    public static boolean is100ContinueExpected(HttpMessage message) {
        return isExpectHeaderValid(message)
                // unquoted tokens in the expect header are case-insensitive, thus 100-continue is case insensitive
                && message.headers().contains(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE, true);
    }

    /**
     * Returns {@code true} if the specified message contains an expect header specifying an expectation that is not
     * supported. Note that this method returns {@code false} if the expect header is not valid for the message
     * (e.g., the message is a response, or the version on the message is HTTP/1.0).
     *
     * @param message the message
     * @return {@code true} if and only if an expectation is present that is not supported
     */
    public static boolean isUnsupportedExpectation(HttpMessage message) {
        if (!isExpectHeaderValid(message)) {
            return false;
        }

        final String expectValue = message.headers().get(HttpHeaderNames.EXPECT);
        return expectValue != null && !HttpHeaderValues.CONTINUE.toString().equalsIgnoreCase(expectValue);
    }

    /**
     * Sets or removes the {@code "Expect: 100-continue"} header to / from the
     * specified message. If {@code expected} is {@code true},
     * the {@code "Expect: 100-continue"} header is set and all other previous
     * {@code "Expect"} headers are removed.  Otherwise, all {@code "Expect"}
     * headers are removed completely.
     */
    public static void set100ContinueExpected(HttpMessage message, boolean expected) {
        if (expected) {
            message.headers().set(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);
        } else {
            message.headers().remove(HttpHeaderNames.EXPECT);
        }
    }

    /**
     * Checks to see if the transfer encoding in a specified {@link HttpMessage} is chunked
     *
     * @param message The message to check
     * @return True if transfer encoding is chunked, otherwise false
     */
    public static boolean isTransferEncodingChunked(HttpMessage message) {
        return message.headers().containsValue(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true);
    }

    /**
     * Set the {@link HttpHeaderNames#TRANSFER_ENCODING} to either include {@link HttpHeaderValues#CHUNKED} if
     * {@code chunked} is {@code true}, or remove {@link HttpHeaderValues#CHUNKED} if {@code chunked} is {@code false}.
     *
     * @param m       The message which contains the headers to modify.
     * @param chunked if {@code true} then include {@link HttpHeaderValues#CHUNKED} in the headers. otherwise remove
     *                {@link HttpHeaderValues#CHUNKED} from the headers.
     */
    public static void setTransferEncodingChunked(HttpMessage m, boolean chunked) {
        if (chunked) {
            m.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            m.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
        } else {
            List<String> encodings = m.headers().getAll(HttpHeaderNames.TRANSFER_ENCODING);
            if (encodings.isEmpty()) {
                return;
            }
            List<CharSequence> values = new ArrayList<CharSequence>(encodings);
            Iterator<CharSequence> valuesIt = values.iterator();
            while (valuesIt.hasNext()) {
                CharSequence value = valuesIt.next();
                if (HttpHeaderValues.CHUNKED.contentEqualsIgnoreCase(value)) {
                    valuesIt.remove();
                }
            }
            if (values.isEmpty()) {
                m.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
            } else {
                m.headers().set(HttpHeaderNames.TRANSFER_ENCODING, values);
            }
        }
    }

    public static StringBuilder appendFullRequest(StringBuilder buf, FullHttpRequest req) {
        appendFullCommon(buf, req);
        appendInitialLine(buf, req);
        appendHeaders(buf, req.headers());
        appendHeaders(buf, req.trailingHeaders());
        removeLastNewLine(buf);
        return buf;
    }

    /**
     * Fetch charset from message's Content-Type header.
     *
     * @param message entity to fetch Content-Type header from
     * @return the charset from message's Content-Type header or {@link CharsetUtil#ISO_8859_1}
     * if charset is not presented or unparsable
     */
    public static Charset getCharset(HttpMessage message) {
        return getCharset(message, CharsetUtil.ISO_8859_1);
    }

    /**
     * Fetch charset from Content-Type header value.
     *
     * @param contentTypeValue Content-Type header value to parse
     * @return the charset from message's Content-Type header or {@link CharsetUtil#ISO_8859_1}
     * if charset is not presented or unparsable
     */
    public static Charset getCharset(CharSequence contentTypeValue) {
        if (contentTypeValue != null) {
            return getCharset(contentTypeValue, CharsetUtil.ISO_8859_1);
        } else {
            return CharsetUtil.ISO_8859_1;
        }
    }

    /**
     * Fetch charset from message's Content-Type header.
     *
     * @param message        entity to fetch Content-Type header from
     * @param defaultCharset result to use in case of empty, incorrect or doesn't contain required part header value
     * @return the charset from message's Content-Type header or {@code defaultCharset}
     * if charset is not presented or unparsable
     */
    public static Charset getCharset(HttpMessage message, Charset defaultCharset) {
        CharSequence contentTypeValue = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeValue != null) {
            return getCharset(contentTypeValue, defaultCharset);
        } else {
            return defaultCharset;
        }
    }

    /**
     * Fetch charset from Content-Type header value.
     *
     * @param contentTypeValue Content-Type header value to parse
     * @param defaultCharset   result to use in case of empty, incorrect or doesn't contain required part header value
     * @return the charset from message's Content-Type header or {@code defaultCharset}
     * if charset is not presented or unparsable
     */
    public static Charset getCharset(CharSequence contentTypeValue, Charset defaultCharset) {
        if (contentTypeValue != null) {
            CharSequence charsetRaw = getCharsetAsSequence(contentTypeValue);
            if (charsetRaw != null) {
                if (charsetRaw.length() > 2) { // at least contains 2 quotes(")
                    if (charsetRaw.charAt(0) == '"' && charsetRaw.charAt(charsetRaw.length() - 1) == '"') {
                        charsetRaw = charsetRaw.subSequence(1, charsetRaw.length() - 1);
                    }
                }
                try {
                    return Charset.forName(charsetRaw.toString());
                } catch (IllegalCharsetNameException ignored) {
                    // just return the default charset
                } catch (UnsupportedCharsetException ignored) {
                    // just return the default charset
                }
            }
        }
        return defaultCharset;
    }

    /**
     * Fetch charset from message's Content-Type header as a char sequence.
     * <p>
     * A lot of sites/possibly clients have charset="CHARSET", for example charset="utf-8". Or "utf8" instead of "utf-8"
     * This is not according to standard, but this method provide an ability to catch desired mistakes manually in code
     *
     * @param message entity to fetch Content-Type header from
     * @return the {@code CharSequence} with charset from message's Content-Type header
     * or {@code null} if charset is not presented
     * @deprecated use {@link #getCharsetAsSequence(HttpMessage)}
     */
    @Deprecated
    public static CharSequence getCharsetAsString(HttpMessage message) {
        return getCharsetAsSequence(message);
    }

    /**
     * Fetch charset from message's Content-Type header as a char sequence.
     * <p>
     * A lot of sites/possibly clients have charset="CHARSET", for example charset="utf-8". Or "utf8" instead of "utf-8"
     * This is not according to standard, but this method provide an ability to catch desired mistakes manually in code
     *
     * @return the {@code CharSequence} with charset from message's Content-Type header
     * or {@code null} if charset is not presented
     */
    public static CharSequence getCharsetAsSequence(HttpMessage message) {
        CharSequence contentTypeValue = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeValue != null) {
            return getCharsetAsSequence(contentTypeValue);
        } else {
            return null;
        }
    }

    /**
     * Fetch charset from Content-Type header value as a char sequence.
     * <p>
     * A lot of sites/possibly clients have charset="CHARSET", for example charset="utf-8". Or "utf8" instead of "utf-8"
     * This is not according to standard, but this method provide an ability to catch desired mistakes manually in code
     *
     * @param contentTypeValue Content-Type header value to parse
     * @return the {@code CharSequence} with charset from message's Content-Type header
     * or {@code null} if charset is not presented
     * @throws NullPointerException in case if {@code contentTypeValue == null}
     */
    public static CharSequence getCharsetAsSequence(CharSequence contentTypeValue) {
        ObjectUtil.checkNotNull(contentTypeValue, "contentTypeValue");

        int indexOfCharset = AsciiString.indexOfIgnoreCaseAscii(contentTypeValue, CHARSET_EQUALS, 0);
        if (indexOfCharset == AsciiString.INDEX_NOT_FOUND) {
            return null;
        }

        int indexOfEncoding = indexOfCharset + CHARSET_EQUALS.length();
        if (indexOfEncoding < contentTypeValue.length()) {
            CharSequence charsetCandidate = contentTypeValue.subSequence(indexOfEncoding, contentTypeValue.length());
            int indexOfSemicolon = AsciiString.indexOfIgnoreCaseAscii(charsetCandidate, SEMICOLON, 0);
            if (indexOfSemicolon == AsciiString.INDEX_NOT_FOUND) {
                return charsetCandidate;
            }

            return charsetCandidate.subSequence(0, indexOfSemicolon);
        }

        return null;
    }

    /**
     * Fetch MIME type part from message's Content-Type header as a char sequence.
     *
     * @param message entity to fetch Content-Type header from
     * @return the MIME type as a {@code CharSequence} from message's Content-Type header
     * or {@code null} if content-type header or MIME type part of this header are not presented
     * <p/>
     * "content-type: text/html; charset=utf-8" - "text/html" will be returned <br/>
     * "content-type: text/html" - "text/html" will be returned <br/>
     * "content-type: " or no header - {@code null} we be returned
     */
    public static CharSequence getMimeType(HttpMessage message) {
        CharSequence contentTypeValue = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeValue != null) {
            return getMimeType(contentTypeValue);
        } else {
            return null;
        }
    }

    /**
     * Fetch MIME type part from Content-Type header value as a char sequence.
     *
     * @param contentTypeValue Content-Type header value to parse
     * @return the MIME type as a {@code CharSequence} from message's Content-Type header
     * or {@code null} if content-type header or MIME type part of this header are not presented
     * <p/>
     * "content-type: text/html; charset=utf-8" - "text/html" will be returned <br/>
     * "content-type: text/html" - "text/html" will be returned <br/>
     * "content-type: empty header - {@code null} we be returned
     * @throws NullPointerException in case if {@code contentTypeValue == null}
     */
    public static CharSequence getMimeType(CharSequence contentTypeValue) {
        ObjectUtil.checkNotNull(contentTypeValue, "contentTypeValue");

        int indexOfSemicolon = AsciiString.indexOfIgnoreCaseAscii(contentTypeValue, SEMICOLON, 0);
        if (indexOfSemicolon != AsciiString.INDEX_NOT_FOUND) {
            return contentTypeValue.subSequence(0, indexOfSemicolon);
        } else {
            return contentTypeValue.length() > 0 ? contentTypeValue : null;
        }
    }

    /**
     * Formats the host string of an address so it can be used for computing an HTTP component
     * such as a URL or a Host header
     *
     * @param addr the address
     * @return the formatted String
     */
    public static String formatHostnameForHttp(InetSocketAddress addr) {
        String hostString = NetUtil.getHostname(addr);
        if (NetUtil.isValidIpV6Address(hostString)) {
            if (!addr.isUnresolved()) {
                hostString = NetUtil.toAddressString(addr.getAddress());
            }
            return '[' + hostString + ']';
        }
        return hostString;
    }

    /**
     * Validates, and optionally extracts the content length from headers. This method is not intended for
     * general use, but is here to be shared between HTTP/1 and HTTP/2 parsing.
     *
     * @param contentLengthFields          the content-length header fields.
     * @param isHttp10OrEarlier            {@code true} if we are handling HTTP/1.0 or earlier
     * @param allowDuplicateContentLengths {@code true}  if multiple, identical-value content lengths should be allowed.
     * @return the normalized content length from the headers or {@code -1} if the fields were empty.
     * @throws IllegalArgumentException if the content-length fields are not valid
     */
    @UnstableApi
    public static long normalizeAndGetContentLength(
            List<? extends CharSequence> contentLengthFields, boolean isHttp10OrEarlier,
            boolean allowDuplicateContentLengths) {
        if (contentLengthFields.isEmpty()) {
            return -1;
        }

        // Guard against multiple Content-Length headers as stated in
        // https://tools.ietf.org/html/rfc7230#section-3.3.2:
        //
        // If a message is received that has multiple Content-Length header
        //   fields with field-values consisting of the same decimal value, or a
        //   single Content-Length header field with a field value containing a
        //   list of identical decimal values (e.g., "Content-Length: 42, 42"),
        //   indicating that duplicate Content-Length header fields have been
        //   generated or combined by an upstream message processor, then the
        //   recipient MUST either reject the message as invalid or replace the
        //   duplicated field-values with a single valid Content-Length field
        //   containing that decimal value prior to determining the message body
        //   length or forwarding the message.
        String firstField = contentLengthFields.get(0).toString();
        boolean multipleContentLengths =
                contentLengthFields.size() > 1 || firstField.indexOf(COMMA) >= 0;

        if (multipleContentLengths && !isHttp10OrEarlier) {
            if (allowDuplicateContentLengths) {
                // Find and enforce that all Content-Length values are the same
                String firstValue = null;
                for (CharSequence field : contentLengthFields) {
                    String[] tokens = field.toString().split(COMMA_STRING, -1);
                    for (String token : tokens) {
                        String trimmed = token.trim();
                        if (firstValue == null) {
                            firstValue = trimmed;
                        } else if (!trimmed.equals(firstValue)) {
                            throw new IllegalArgumentException(
                                    "Multiple Content-Length values found: " + contentLengthFields);
                        }
                    }
                }
                // Replace the duplicated field-values with a single valid Content-Length field
                firstField = firstValue;
            } else {
                // Reject the message as invalid
                throw new IllegalArgumentException(
                        "Multiple Content-Length values found: " + contentLengthFields);
            }
        }
        // Ensure we not allow sign as part of the content-length:
        // See https://github.com/squid-cache/squid/security/advisories/GHSA-qf3v-rc95-96j5
        if (firstField.isEmpty() || !Character.isDigit(firstField.charAt(0))) {
            // Reject the message as invalid
            throw new IllegalArgumentException(
                    "Content-Length value is not a number: " + firstField);
        }
        try {
            final long value = Long.parseLong(firstField);
            return checkPositiveOrZero(value, "Content-Length value");
        } catch (NumberFormatException e) {
            // Reject the message as invalid
            throw new IllegalArgumentException(
                    "Content-Length value is not a number: " + firstField, e);
        }
    }

    public static String path(final String uri) {
        if (uri.length() == 0) {
            return "";
        }
        int i;
        if (uri.charAt(0) == '/') {
            i = 0;
        } else {
            i = uri.indexOf("://");
            if (i == -1) {
                i = 0;
            } else {
                i = uri.indexOf('/', i + 3);
                if (i == -1) {
                    // contains no /
                    return "/";
                }
            }
        }

        int queryStart = uri.indexOf('?', i);
        if (queryStart == -1) {
            queryStart = uri.length();
        }
        return uri.substring(i, queryStart);
    }

    public static String query(final String uri) {
        int i = uri.indexOf('?');
        if (i == -1) {
            return null;
        } else {
            return uri.substring(i + 1);
        }
    }

    static StringBuilder appendRequest(StringBuilder buf, HttpRequest req) {
        appendCommon(buf, req);
        appendInitialLine(buf, req);
        appendHeaders(buf, req.headers());
        removeLastNewLine(buf);
        return buf;
    }

    static StringBuilder appendResponse(StringBuilder buf, HttpResponse res) {
        appendCommon(buf, res);
        appendInitialLine(buf, res);
        appendHeaders(buf, res.headers());
        removeLastNewLine(buf);
        return buf;
    }

    static StringBuilder appendFullResponse(StringBuilder buf, FullHttpResponse res) {
        appendFullCommon(buf, res);
        appendInitialLine(buf, res);
        appendHeaders(buf, res.headers());
        appendHeaders(buf, res.trailingHeaders());
        removeLastNewLine(buf);
        return buf;
    }

    /**
     * Returns the content length of the specified web socket message. If the
     * specified message is not a web socket message, {@code -1} is returned.
     */
    private static int getWebSocketContentLength(HttpMessage message) {
        // WebSocket messages have constant content-lengths.
        HttpHeaders h = message.headers();
        if (message instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) message;
            if (HttpMethod.GET.equals(req.method()) &&
                    h.contains(HttpHeaderNames.SEC_WEBSOCKET_KEY1) &&
                    h.contains(HttpHeaderNames.SEC_WEBSOCKET_KEY2)) {
                return 8;
            }
        } else if (message instanceof HttpResponse) {
            HttpResponse res = (HttpResponse) message;
            if (res.status().code() == 101 &&
                    h.contains(HttpHeaderNames.SEC_WEBSOCKET_ORIGIN) &&
                    h.contains(HttpHeaderNames.SEC_WEBSOCKET_LOCATION)) {
                return 16;
            }
        }

        // Not a web socket message
        return -1;
    }

    private static boolean isExpectHeaderValid(final HttpMessage message) {
        /*
         * Expect: 100-continue is for requests only and it works only on HTTP/1.1 or later. Note further that RFC 7231
         * section 5.1.1 says "A server that receives a 100-continue expectation in an HTTP/1.0 request MUST ignore
         * that expectation."
         */
        return message instanceof HttpRequest &&
                message.protocolVersion().compareTo(HttpVersion.HTTP_1_1) >= 0;
    }

    private static void appendCommon(StringBuilder buf, HttpMessage msg) {
        buf.append(StringUtil.simpleClassName(msg));
        buf.append("(decodeResult: ");
        buf.append(msg.decoderResult());
        buf.append(", version: ");
        buf.append(msg.protocolVersion());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendFullCommon(StringBuilder buf, FullHttpMessage msg) {
        buf.append(StringUtil.simpleClassName(msg));
        buf.append("(decodeResult: ");
        buf.append(msg.decoderResult());
        buf.append(", version: ");
        buf.append(msg.protocolVersion());
        buf.append(", content: ");
        buf.append(msg.content());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendInitialLine(StringBuilder buf, HttpRequest req) {
        buf.append(req.method());
        buf.append(' ');
        buf.append(req.uri());
        buf.append(' ');
        buf.append(req.protocolVersion());
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendInitialLine(StringBuilder buf, HttpResponse res) {
        buf.append(res.protocolVersion());
        buf.append(' ');
        buf.append(res.status());
        buf.append(StringUtil.NEWLINE);
    }

    private static void appendHeaders(StringBuilder buf, HttpHeaders headers) {
        for (Map.Entry<String, String> e : headers) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }

    private static void removeLastNewLine(StringBuilder buf) {
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
    }

    public final static class InboundIdleStateHandler extends IdleStateHandler {

        final Runnable onReadIdle;

        public InboundIdleStateHandler(long idleTimeout, Runnable onReadIdle) {
            super(idleTimeout, 0, 0, TimeUnit.MILLISECONDS);
            this.onReadIdle = requireNonNull(onReadIdle, "onReadIdle");
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx,
                                   IdleStateEvent evt) throws Exception {
            if (evt.state() == IdleState.READER_IDLE) {
                onReadIdle.run();
            }
            super.channelIdle(ctx, evt);
        }
    }

    public final static class OutboundIdleStateHandler extends IdleStateHandler {

        final Runnable onWriteIdle;

        public OutboundIdleStateHandler(long idleTimeout, Runnable onWriteIdle) {
            super(0, idleTimeout, 0, TimeUnit.MILLISECONDS);
            this.onWriteIdle = requireNonNull(onWriteIdle, "onWriteIdle");
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx,
                                   IdleStateEvent evt) throws Exception {
            if (evt.state() == IdleState.WRITER_IDLE) {
                onWriteIdle.run();
            }
            super.channelIdle(ctx, evt);
        }
    }

    @ChannelHandler.Sharable
    public static final class ExtractorHandler extends ChannelInboundHandlerAdapter {


        final BiConsumer<? super ChannelHandlerContext, Object> extractor;

        public ExtractorHandler(BiConsumer<? super ChannelHandlerContext, Object> extractor) {
            this.extractor = Objects.requireNonNull(extractor, "extractor");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            extractor.accept(ctx, msg);
        }

    }

}