package org.atomicode.inf.route.inter;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 11:55
 */
public interface Routing<Target> {

    int index();

    String path();

    RequestMethod method();

    Target target();

    SignalType type();

    boolean matches(RequestMethod otherMethodType, String otherMethodPath);

}