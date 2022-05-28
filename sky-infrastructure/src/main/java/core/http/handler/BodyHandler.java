// package core.http.handler;
//
// import core.http.inter.AggHttpServerRequest;
// import io.netty.channel.ChannelHandlerContext;
// import io.netty.channel.ChannelInboundHandlerAdapter;
// import io.netty.handler.codec.http.DefaultHttpRequest;
// import io.netty.handler.codec.http.FullHttpMessage;
// import io.netty.handler.codec.http.HttpContent;
// import io.netty.handler.codec.http.HttpMessage;
// import io.netty.handler.codec.http.HttpObject;
// import io.netty.handler.codec.http.HttpRequest;
// import io.netty.handler.codec.http.LastHttpContent;
// import org.jetbrains.annotations.NotNull;
// import util.Netty;
//
// import static io.netty.handler.codec.http.HttpUtil.getContentLength;
//
// /**
//  * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
//  * @date 2022/5/28 20:45
//  */
// public class BodyHandler extends ChannelInboundHandlerAdapter {
//
//     private AggHttpServerRequest currentRequest;
//
//     public static BodyHandler create() {
//         return new BodyHandler();
//     }
//
//     @Override
//     public void channelRead(@NotNull final ChannelHandlerContext ctx, @NotNull final Object msg) throws Exception {
//         if (msg instanceof HttpRequest) {
//             currentRequest = new AggHttpServerRequest((HttpRequest) msg, Netty.empty);
//         }
//
//         } else if (isContentMessage(msg)) {
//             final HttpContent content = (HttpContent) msg;
//             if (isContentMessage(content)) {
//                 currentRequest.offer(content);
//             }
//
//             if (isLastContentMessage(content)) {
//                 currentRequest.addLastHttpContent((LastHttpContent) msg);
//             }
//         }
//
//         if (isAggregated(msg)) {
//             currentRequest.aggregate();
//             ctx.fireChannelRead(currentRequest);
//             currentRequest = null;
//         }
//     }
//
//
//     protected boolean isStartMessage(Object msg) throws Exception {
//         return msg instanceof DefaultHttpRequest;
//     }
//
//     protected boolean isContentMessage(Object msg) throws Exception {
//         return msg instanceof HttpContent;
//     }
//
//     protected boolean isLastContentMessage(HttpContent msg) throws Exception {
//         return msg instanceof LastHttpContent;
//     }
//
//     protected boolean isAggregated(HttpObject msg) throws Exception {
//         return msg instanceof FullHttpMessage;
//     }
//
//     protected boolean isContentLengthInvalid(HttpMessage start, int maxContentLength) {
//         try {
//             return getContentLength(start, -1L) > maxContentLength;
//         } catch (final NumberFormatException e) {
//             return false;
//         }
//     }
// }