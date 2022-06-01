package core.http.handler;

import core.http.ext.AccessLogMapper;
import core.http.inter.AccessLogArgsBuilder;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.anno.NotNull;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;


/**
 * print access log.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/31 16:21
 */
public class AccessLogHandler extends ChannelDuplexHandler {

    private final AccessLogMapper mapper;
    private AccessLogArgsBuilder builder;

    public AccessLogHandler(final AccessLogMapper mapper) { this.mapper = mapper; }

    @Override
    public void channelRead(@NotNull final ChannelHandlerContext ctx, @NotNull final Object msg) throws Exception {
        if ((msg instanceof HttpRequest)) {
            final HttpRequest req = (HttpRequest) msg;

            if (Lang.isNull(builder)) {
                builder = new AccessLogArgsBuilder(ctx.channel().remoteAddress());
            }
            builder.mountRequest(req);
        }

        ctx.fireChannelRead(msg);
    }


    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if ((msg instanceof HttpResponse)) {
            System.out.println("accessLog response");
        }

        if (msg instanceof LastHttpContent) {
            System.out.println("accessLog lastHttpContent");
        }

        ctx.write(msg, promise);
    }
}