package core.http.ext;

import core.http.inter.AccessLog;
import core.http.inter.AccessLogArgs;

import java.util.function.Function;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/6/1 14:34
 */
@FunctionalInterface
public interface AccessLogMapper extends Function<AccessLogArgs, AccessLog> {

    public static AccessLogMapper MAPPER = new AccessLogMapper() {
        @Override
        public AccessLog apply(final AccessLogArgs accessLogArgs) {
            return null;
        }
    };
}