package io.github.fzdwx.inf.http.inter;

import ch.qos.logback.core.util.ContentTypeUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.ContentType;
import io.github.fzdwx.inf.http.core.HttpResponse;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.stream.ChunkedStream;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static io.github.fzdwx.inf.Netty.close;
import static io.github.fzdwx.inf.http.core.ContentType.JSON;
import static io.github.fzdwx.lambada.Lang.CHARSET;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:27
 */
public class HttpResponseImpl implements HttpResponse {

    public final Channel channel;
    private String contentType;
    private String contentDisposition;

    public HttpResponseImpl(final Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    public HttpResponse contentType(final String contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpResponse contentDisposition(String fileName) {
        if (fileName == null) return this;

        this.contentDisposition = "attachment; filename=" + fileName;
        return this;
    }

    public HttpResponse contentDispositionFull(String contentDisposition) {
        if (contentDisposition == null) return this;

        this.contentDisposition = contentDisposition;
        return this;
    }

    @Override
    public ChannelFuture reject() {
        return channel.close();
    }

    @Override
    public ChannelFuture reject(final HttpMessage result) {
        return channel.writeAndFlush(result).addListener(close);
    }

    @Override
    public void redirect(final String url) {
        redirect(url, c -> c.addListener(f -> channel.flush().close()));
    }

    @Override
    public void redirect(final String url, final Hooks<ChannelFuture> h) {
        h.call(channel.write(HttpResult.redirect(url)));
    }

    @Override
    public void json(final String json) {
        json(json.getBytes());
    }

    @Override
    public void json(final String json, final Hooks<ChannelFuture> h) {
        json(json.getBytes(CHARSET), h);
    }

    @Override
    public void json(final byte[] json) {
        json(json, f -> f.addListener(close));
    }

    @Override
    public void json(final byte[] json, final Hooks<ChannelFuture> h) {
        contentType = JSON;
        bytes(json, h);
    }

    @Override
    public void html(final String html) {
        html(html, f -> f.addListener(close));
    }

    @Override
    public void html(final String html, final Hooks<ChannelFuture> h) {
        contentType = ContentType.TEXT_HTML;
        bytes(html.getBytes(CHARSET), h);
    }

    @Override
    public void file(final String filePath) {
        file(new File(filePath));
    }

    @Override
    public void file(final String filePath, final Hooks<ChannelFuture> h) {
        file(new File(filePath), h);
    }

    @Override
    public void file(final File file) {
        file(file, c -> c.addListener(f -> channel.flush().close()));
    }

    @SneakyThrows
    @Override
    public void file(final File file, final Hooks<ChannelFuture> h) {
        if (file.isHidden() || !file.exists()) {
            this.reject(HttpResult.NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            this.reject(HttpResult.fail("It is Directory"));
            return;
        }

        if (!file.isFile()) {
            this.reject(HttpResult.fail("It is not File"));
            return;
        }

        if (file.length() <= Netty.DEFAULT_CHUNK_SIZE) {
            this.bytes(FileUtil.readBytes(file), h);
            return;
        }

        output(new FileInputStream(file), h);
    }

    @Override
    public void output(final String str) {
        bytes(str.getBytes(CHARSET));
    }

    @Override
    public void output(final String str, final Hooks<ChannelFuture> h) {
        bytes(str.getBytes(CHARSET), h);
    }

    @Override
    public void bytes(final byte[] bytes) {
        bytes(bytes, c -> c.addListener(f -> channel.flush().close()));
    }

    @Override
    public void bytes(final byte[] bytes, final Hooks<ChannelFuture> h) {
        if (bytes.length <= Netty.DEFAULT_CHUNK_SIZE) {
            h.call(channel.write(HttpResult.ok(bytes).contentType(contentType).contentDisposition(contentDisposition)));
            return;
        }

        // 分片执行
        output(new ByteArrayInputStream(bytes), h);
    }

    @SneakyThrows
    @Override
    public void output(final InputStream stream) {
        output(stream, f -> f.addListener(close));
    }

    @SneakyThrows
    @Override
    public void output(final InputStream stream, final Hooks<ChannelFuture> h) {
        final var httpResult = SimpleHttpResult.empty()
                .contentType(contentType)
                .contentDispositionFull(contentDisposition)
                .contentLength(stream.available());

        output(httpResult, f -> h.call(channel.writeAndFlush(new HttpChunkedInput(new ChunkedStream(stream, Netty.DEFAULT_CHUNK_SIZE)))));
    }

    @Override
    public void output(final HttpMessage result) {
        output(result, c -> c.addListener(f -> {
            channel.flush().close();
        }));
    }

    @Override
    public void output(final HttpMessage result, final Hooks<ChannelFuture> h) {
        h.call(channel.write(result));
    }
}