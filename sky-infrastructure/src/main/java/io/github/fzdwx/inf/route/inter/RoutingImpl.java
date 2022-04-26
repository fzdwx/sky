package io.github.fzdwx.inf.route.inter;

import io.github.fzdwx.lambada.lang.PathAnalyzer;

/**
 * @author <a href="mailto:likelovec@gmail.com">韦朕</a>
 * @date 2022/3/18 12:04
 */
public class RoutingImpl<Target> implements Routing<Target> {

    private final PathAnalyzer rule; //path rule 规则
    private final int index; //顺序
    private final String path; //path
    private final Target target;//代理
    private final RequestMethod method; //方式
    private final SignalType type; //信号

    public RoutingImpl(final int index, final String path, final Target target, final RequestMethod method) {
        this.index = index;
        this.path = path;
        this.target = target;
        this.method = method;
        this.type = method.signal;
        this.rule = PathAnalyzer.get(path);
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Target target() {
        return target;
    }

    @Override
    public SignalType type() {
        return null;
    }

    @Override
    public RequestMethod method() {
        return method;
    }

    /**
     * 是否匹配
     */
    @Override
    public boolean matches(RequestMethod method2, String path2) {
        if (RequestMethod.ALL == method) {
            return matches0(path2);
        } else if (RequestMethod.HTTP == method) { //不是null时，不能用==
            if (method2.signal == SignalType.HTTP) {
                return matches0(path2);
            }
        } else if (method2 == method) {
            return matches0(path2);
        }

        return false;
    }

    private boolean matches0(String path2) {
        // 1.如果当前为**，任何路径都可命中
        if ("**".equals(path) || "/**".equals(path)) {
            return true;
        }

        // 2.如果与当前路径相关
        if (path.equals(path2)) {
            return true;
        }

        // 3.ues rule check
        if (this.rule.matches(path2)) {
            return true;
        }

        // 4.正则检测
        return rule.matches(path2);
    }
}