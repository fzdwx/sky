package core.http.handler;

import core.http.ext.HttpHandler;
import core.http.ext.HttpServerRequest;
import core.http.ext.HttpServerResponse;
import io.github.fzdwx.lambada.Io;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import util.Netty;

/**
 * impl of {@link HttpHandler} for serving static files
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/28 23:25
 */
public class StaticFileHandler implements HttpHandler {

    private StaticFileHandler() {
    }

    @Override
    public void handle(final HttpServerRequest request, final HttpServerResponse response) {
        response.sendFile(Io.newRaf("C:\\Users\\98065\\Desktop\\221.5080.210.7z"), Netty.DEFAULT_CHUNK_SIZE, false, (new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(final ChannelProgressiveFuture future, final long progress, final long total) throws Exception {
                if (total < 0) { // total unknown
                    System.err.println(future.channel() + " Transfer progress: " + progress);
                } else {
                    System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
                }
            }

            @Override
            public void operationComplete(final ChannelProgressiveFuture future) throws Exception {
                System.err.println(future.channel() + " Transfer complete.");
            }
        })).addListener(f -> {
            System.out.println("f.cause() = " + f.cause());
        });
    }

    public static HttpHandler create(final String s, final String s1) {
        return new StaticFileHandler();
    }
}