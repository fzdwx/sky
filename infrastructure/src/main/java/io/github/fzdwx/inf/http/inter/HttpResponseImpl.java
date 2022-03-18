package io.github.fzdwx.inf.http.inter;

import cn.hutool.core.io.FileUtil;
import io.github.fzdwx.inf.Netty;
import io.github.fzdwx.inf.http.HttpResponse;
import io.netty.channel.Channel;

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
    public void json(final String json) {
        json(json.getBytes());
    }

    @Override
    public void json(final byte[] json) {
        channel.writeAndFlush(HttpResult.ok(json).json()).addListener(Netty.close);
    }

    @Override
    public void html(final String html) {
        channel.writeAndFlush(HttpResult.ok(html)).addListener(Netty.close);
    }

    @Override
    public void bytes(final byte[] bytes) {
        channel.writeAndFlush(HttpResult.ok(bytes)).addListener(Netty.close);
    }

    @Override
    public void redirect(final String url) {
        channel.writeAndFlush(HttpResult.redirect(url)).addListener(Netty.close);
    }

    @Override
    public void file(final File file) {
        file(FileUtil.readBytes(file), file.getName());
    }

    @Override
    public void file(final String filePath) {
        file(new File(filePath));
    }

    @Override
    public void file(final byte[] bytes, final String fileName) {
        channel.writeAndFlush(HttpResult.file(bytes, fileName)).addListener(Netty.close);
    }
}