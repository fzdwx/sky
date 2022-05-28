package core.thread;

import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/27 18:39
 */
public class SkyThreadFactory extends DefaultThreadFactory {

    private String ext;
    private final String PREFIX = "SKY-";

    public SkyThreadFactory(final String poolName) {
        super(poolName);
    }

    @Override
    protected Thread newThread(final Runnable r, final String name) {
        final Thread thread = super.newThread(r, name);
        thread.setName(PREFIX + ext + thread.getName());
        return thread;
    }
}