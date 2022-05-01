package io.github.fzdwx.inf.msg;

import io.github.fzdwx.inf.route.msg.SocketSession;
import io.github.fzdwx.lambada.lang.PathAnalyzer;
import io.github.fzdwx.lambada.lang.PathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 14:51
 */
public class ListenerWrapper implements Listener {

    private Listener listener;
    //path 分析器
    private PathAnalyzer pathAnalyzer;//路径分析器
    //path key 列表
    private List<String> pathKeys;

    public ListenerWrapper(String path, Listener listener) {
        this.listener = listener;

        if (path != null && path.contains("{")) {
            path = PathUtil.mergePath(null, path);

            pathKeys = new ArrayList<>();
            Matcher pm = PathUtil.pathKeyExpr.matcher(path);
            while (pm.find()) {
                pathKeys.add(pm.group(1));
            }

            if (pathKeys.size() > 0) {
                pathAnalyzer = PathAnalyzer.get(path);
            }
        }
    }

    @Override
    public void onText(SocketSession session, final String text) {
        listener.onText(session, text);
    }
}