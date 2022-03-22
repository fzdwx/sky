package io.github.fzdwx.helloworld.client;

import io.github.fzdwx.inf.ClientInf;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/17 16:19
 */
public class Client extends ClientInf<Client> {

    public Client(final int port) {
        super(port);
    }

    @Override
    public Hooks<SocketChannel> registerInitChannel() {
        return ch -> {
            ch.pipeline()
                    // 以("\n")为结尾分割的 解码器,防止粘包
                    .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                    // 解码和编码，应和客户端一致
                    .addLast(new StringDecoder())
                    .addLast(new StringEncoder())
                    .addLast(new ClientHandler());
        };
    }

    @Override
    public @NonNull Class<? extends SocketChannel> channelClassType() {
        return NioSocketChannel.class;
    }

    @Override
    protected Client me() {
        return this;
    }

    @Override
    public void addClientOptions(final Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    }

    @SneakyThrows
    public static void main(String[] args) {
        final var client = new Client(8080);
        try {
            client.bind();
            final var ch = client.channel();
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(line + "\r\n");
                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equalsIgnoreCase(line)) {
                    ch.closeFuture().sync();
                    break;
                }
            }
            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            client.stop();
        }
    }
}