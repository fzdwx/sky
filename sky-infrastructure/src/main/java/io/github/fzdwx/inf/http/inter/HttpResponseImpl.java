package io.github.fzdwx.inf.http.inter;

import cn.hutool.core.io.FileUtil;
import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.core.HttpResponse;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.File;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 15:27
 */
public class HttpResponseImpl implements HttpResponse {

    public final Channel channel;

    public HttpResponseImpl(final Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public void json(final String json) {
        json(json.getBytes());
    }

    @Override
    public void json(final byte[] json) {
        json(json, c -> c.addListener(Netty.close));
    }

    @Override
    public void json(final String json, final Hooks<ChannelFuture> h) {
        json(json.getBytes(), h);
    }

    @Override
    public void json(final byte[] json, final Hooks<ChannelFuture> h) {
        h.call(channel.writeAndFlush(HttpResult.ok(json).json()));
    }

    @Override
    public void html(final String html) {
        html(html, c -> c.addListener(Netty.close));
    }

    @Override
    public void html(final String html, final Hooks<ChannelFuture> h) {
        h.call(channel.writeAndFlush(HttpResult.ok(html)));
    }

    @Override
    public void bytes(final byte[] bytes) {
        bytes(bytes, c -> c.addListener(Netty.close));
    }

    @Override
    public void bytes(final byte[] bytes, final Hooks<ChannelFuture> h) {
        h.call(channel.writeAndFlush(HttpResult.ok(bytes)));
    }

    @Override
    public void redirect(final String url) {
        redirect(url, c -> c.addListener(Netty.close));
    }

    @Override
    public void redirect(final String url, final Hooks<ChannelFuture> h) {
        h.call(channel.writeAndFlush(HttpResult.redirect(url)));
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
        file(file, c -> c.addListener(Netty.close));
    }

    @Override
    public void file(final File file, final Hooks<ChannelFuture> h) {
        file(FileUtil.readBytes(file), file.getName(), h);
    }

    @Override
    public void file(final byte[] bytes, final String fileName) {
        file(bytes, fileName, c -> c.addListener(Netty.close));
    }

    @Override
    public void file(final byte[] bytes, final String fileName, final Hooks<ChannelFuture> h) {
        h.call(channel.writeAndFlush(HttpResult.file(bytes, fileName)));
    }

    @Override
    public void output(final String str, final String contentType) {
        output(str, contentType, c -> c.addListener(Netty.close));
    }

    @Override
    public void output(final String str, final String contentType, final Hooks<ChannelFuture> h) {
        h.call(channel.writeAndFlush(HttpResult.ok(str).contentType(contentType)));
    }
}