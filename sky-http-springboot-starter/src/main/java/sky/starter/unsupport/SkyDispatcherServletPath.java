package sky.starter.unsupport;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 22:37
 */
public class SkyDispatcherServletPath implements DispatcherServletPath {

    private final String path;

    public SkyDispatcherServletPath(final String path) { this.path = path; }

    @Override
    public String getPath() {
        return path;
    }
}