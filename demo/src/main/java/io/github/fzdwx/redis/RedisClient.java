package io.github.fzdwx.redis;

import io.github.fzdwx.inf.ClientInf;
import io.github.fzdwx.lambada.fun.Hooks;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisBulkStringAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;
import lombok.SneakyThrows;

/**
 * SENTINEL get-master-addr-by-name + sentinelMasterName
 *
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/22 16:10
 */
public class RedisClient extends ClientInf<RedisClient> {

    public RedisClient(final String host, final int port) {
        super(host, port);
    }

    @SneakyThrows
    public static RedisClient connect(String host, int port) {
        return new RedisClient(host, port);
    }

    @Override
    public Hooks<SocketChannel> registerInitChannel() {
        return ch -> {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new RedisDecoder());
            p.addLast(new RedisBulkStringAggregator());
            p.addLast(new RedisArrayAggregator());
            p.addLast(new RedisEncoder());
            p.addLast(new RedisClientHandler());
        };
    }

    @Override
    protected RedisClient me() {
        return this;
    }

    @Override
    public void addClientOptions(final Bootstrap bootstrap) {

    }

    public static void main(String[] args) throws Exception {
        RedisClient.connect("192.168.1.228", 6380)
                .bind(h -> {

                })
                .scanner();
    }
}