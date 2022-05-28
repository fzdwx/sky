package core.http.inter;

import core.http.ext.HttpServerRequest;
import core.serializer.JsonSerializer;
import core.socket.WebSocket;
import io.github.fzdwx.lambada.Seq;
import io.github.fzdwx.lambada.fun.Hooks;
import io.github.fzdwx.lambada.http.HttpMethod;
import io.github.fzdwx.lambada.lang.NvMap;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostStandardRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import util.Netty;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * 聚合 http content数据，并进行解析。
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/28 17:33
 */
public class AggHttpServerRequest extends AggregatedFullHttpMessage implements HttpServerRequest, FullHttpRequest {

    private boolean multipartFlag;
    private boolean formUrlEncoderFlag = false;
    private volatile InterfaceHttpPostRequestDecoder postRequestDecoder;

    public AggHttpServerRequest(final HttpRequest nettyRequest, ByteBuf content) {
        super(nettyRequest, content, null);
        this.multipartFlag = HttpPostRequestDecoder.isMultipart(nettyRequest);
        if (!this.multipartFlag) {
            String contentType = this.contentType();
            this.formUrlEncoderFlag = contentType != null && Netty.isFormUrlEncoder(contentType.toLowerCase());
        }
    }

    private final Supplier<InterfaceHttpPostRequestDecoder> postRequestDecoderSupplier = () -> {
        if (!multipartFlag && !formUrlEncoderFlag) {
            return null;
        }

        if (this.postRequestDecoder == null) {
            synchronized (this) {
                if (this.postRequestDecoder == null) {
                    Charset charset = Netty.getCharset(message);
                    HttpDataFactory httpDataFactory = Netty.dataFactory(charset);
                    InterfaceHttpPostRequestDecoder postRequestDecoder;
                    if (multipartFlag) {
                        postRequestDecoder = new HttpPostMultipartRequestDecoder(httpDataFactory, message, charset);
                    } else if (formUrlEncoderFlag) {
                        postRequestDecoder = new HttpPostStandardRequestDecoder(httpDataFactory, message, charset);
                    } else {
                        throw new HttpPostRequestDecoder.ErrorDataDecoderException();
                    }
                    this.postRequestDecoder = postRequestDecoder;
                }
            }
        }
        return this.postRequestDecoder;
    };

    public void offer(HttpContent httpContent) {
        postRequestDecoderSupplier.get().offer(httpContent);
    }

    @Override
    public boolean multipart() {
        return this.multipartFlag;
    }

    @Override
    public boolean formUrlEncoder() {
        return this.formUrlEncoderFlag;
    }

    @Override
    public SocketAddress remoteAddress() {
        return null;
    }

    @Override
    public HttpVersion version() {
        return this.message.protocolVersion();
    }

    @Override
    public HttpHeaders headers() {
        return this.message.headers();
    }

    @Override
    public NvMap params() {
        return null;
    }

    @Override
    public boolean ssl() {
        return false;
    }

    @Override
    public JsonSerializer serializer() {
        return null;
    }

    @Override
    public ByteBuf readJson() {
        return null;
    }

    @Override
    public Attribute readBody(final String key) {
        return null;
    }

    @Override
    public FileUpload readFile(final String key) {
        return null;
    }

    @Override
    public Seq<FileUpload> readFiles() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public HttpMethod methodType() {
        return null;
    }

    @Override
    public void upgradeToWebSocket(final Hooks<WebSocket> h) {

    }

    @Override
    public boolean isWebsocket() {
        return false;
    }

    @Override
    public HttpRequest nettyRequest() {
        return message;
    }

    @Override
    public String contentType() {
        return message.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public FullHttpRequest copy() {
        return replace(content().copy());
    }

    @Override
    public FullHttpRequest duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public FullHttpRequest retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public FullHttpRequest replace(ByteBuf content) {
        DefaultFullHttpRequest dup = new DefaultFullHttpRequest(protocolVersion(), method(), uri(), content,
                headers().copy(), trailingHeaders().copy());
        dup.setDecoderResult(decoderResult());
        return dup;
    }

    @Override
    public FullHttpRequest retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public FullHttpRequest retain() {
        super.retain();
        return this;
    }

    @Override
    public FullHttpRequest touch() {
        super.touch();
        return this;
    }

    @Override
    public FullHttpRequest touch(Object hint) {
        super.touch(hint);
        return this;
    }

    @Override
    public FullHttpRequest setMethod(io.netty.handler.codec.http.HttpMethod method) {
        ((HttpRequest) message).setMethod(method);
        return this;
    }

    @Override
    public FullHttpRequest setUri(String uri) {
        ((HttpRequest) message).setUri(uri);
        return this;
    }

    @Override
    public io.netty.handler.codec.http.HttpMethod getMethod() {
        return ((HttpRequest) message).method();
    }

    @Override
    public String getUri() {
        return ((HttpRequest) message).uri();
    }

    @Override
    public io.netty.handler.codec.http.HttpMethod method() {
        return getMethod();
    }

    @Override
    public String uri() {
        return getUri();
    }

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public String toString() {
        return Netty.appendFullRequest(new StringBuilder(256), this).toString();
    }

    public InterfaceHttpPostRequestDecoder bodyDecoder() {
        return this.postRequestDecoder;
    }
}