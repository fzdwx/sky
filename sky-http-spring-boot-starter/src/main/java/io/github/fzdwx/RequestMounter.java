package io.github.fzdwx;

import io.github.fzdwx.inf.route.Router;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/29 14:17
 */
public interface RequestMounter {

    void mount(Router router);
}