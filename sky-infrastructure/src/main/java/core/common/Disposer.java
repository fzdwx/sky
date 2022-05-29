package core.common;

import io.netty.channel.ChannelFuture;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/29 8:10
 */
public interface Disposer {

    ChannelFuture dispose();
}