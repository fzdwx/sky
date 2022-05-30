package core.thread;

import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/27 18:39
 */
public class SkyThreadFactory extends DefaultThreadFactory {

    private final String PREFIX = "Sky-";

    public SkyThreadFactory(final String poolName) {
        super(poolName);
    }

    @Override
    protected Thread newThread(final Runnable r, final String name) {
        final Thread thread = super.newThread(r, name);
        thread.setName(PREFIX + thread.getName());
        return thread;
    }
}