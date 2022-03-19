package io.github.fzdwx.helloworld.serv;

import io.github.fzdwx.inf.ServInf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.NonNull;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 14:33
 */
public class Serv extends ServInf<Serv> {

    public Serv(final int port) {
        super(port);
    }

    @Override
    public @NonNull ChannelInitializer<SocketChannel> addChildHandler() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                ch.pipeline()
                        // 以("\n")为结尾分割的 解码器,防止粘包
                        .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                        // 解码和编码，应和客户端一致
                        .addLast(new StringDecoder())
                        .addLast(new StringEncoder())
                        .addLast(new ServHandler());
            }
        };
    }

    @Override
    public @NonNull Class<? extends ServerChannel> serverChannelClass() {
        return NioServerSocketChannel.class;
    }

    @Override
    protected Serv me() {
        return this;
    }

    public static void main(String[] args) {
        new Serv(8080).bind();
    }
}