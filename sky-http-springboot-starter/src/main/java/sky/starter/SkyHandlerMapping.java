package sky.starter;

import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;
import org.springframework.web.servlet.handler.RequestMatchResult;

import javax.servlet.http.HttpServletRequest;

/**
 * sky handler mapping
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 12:27
 */
public class SkyHandlerMapping implements MatchableHandlerMapping {

    @Override
    public RequestMatchResult match(final HttpServletRequest request, final String pattern) {
        return null;
    }

    @Override
    public HandlerExecutionChain getHandler(final HttpServletRequest request) throws Exception {
        return null;
    }
}