package core.http.inter;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public abstract class AggregatedFullHttpMessage implements FullHttpMessage {

    protected final HttpRequest message;
    protected ByteBuf content;
    private HttpHeaders trailingHeaders;

    AggregatedFullHttpMessage(HttpRequest message, ByteBuf content, HttpHeaders trailingHeaders) {
        this.message = message;
        this.content = content;
        this.trailingHeaders = trailingHeaders;
    }

    @Override
    public HttpHeaders trailingHeaders() {
        HttpHeaders trailingHeaders = this.trailingHeaders;
        if (trailingHeaders == null) {
            return EmptyHttpHeaders.INSTANCE;
        } else {
            return trailingHeaders;
        }
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return message.protocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        return message.protocolVersion();
    }

    @Override
    public FullHttpMessage setProtocolVersion(HttpVersion version) {
        message.setProtocolVersion(version);
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return message.headers();
    }

    @Override
    public DecoderResult decoderResult() {
        return message.decoderResult();
    }

    @Override
    public DecoderResult getDecoderResult() {
        return message.decoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        message.setDecoderResult(result);
    }

    @Override
    public ByteBuf content() {
        return content;
    }

    @Override
    public int refCnt() {
        return content.refCnt();
    }

    @Override
    public boolean release() {
        return content.release();
    }

    @Override
    public boolean release(int decrement) {
        return content.release(decrement);
    }

    @Override
    public abstract FullHttpMessage copy();

    @Override
    public abstract FullHttpMessage duplicate();

    @Override
    public abstract FullHttpMessage retainedDuplicate();

    @Override
    public FullHttpMessage retain(int increment) {
        content.retain(increment);
        return this;
    }

    @Override
    public FullHttpMessage retain() {
        content.retain();
        return this;
    }

    @Override
    public FullHttpMessage touch() {
        content.touch();
        return this;
    }

    @Override
    public FullHttpMessage touch(Object hint) {
        content.touch(hint);
        return this;
    }

    void setTrailingHeaders(HttpHeaders trailingHeaders) {
        this.trailingHeaders = trailingHeaders;
    }
}