package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Handler;
import io.github.fzdwx.inf.http.HttpRequest;
import io.github.fzdwx.inf.http.HttpResponse;
import io.github.fzdwx.inf.route.Router;
import lombok.extern.slf4j.Slf4j;

import static cn.hutool.core.io.FileUtil.listFileNames;
import static cn.hutool.core.io.FileUtil.readString;
import static cn.hutool.core.text.CharSequenceUtil.padAfter;
import static io.github.fzdwx.lambada.Lang.CHARSET;
import static io.github.fzdwx.lambada.Seq.of;
import static java.util.stream.Collectors.joining;

/**
 * dev html.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 21:47
 */
@Slf4j
public class HttpDevHtml implements Handler {

    private final String name;
    private String apiList;
    private String fileList;
    public static final String PAGE_PATH = "/dev";

    public HttpDevHtml(final String name, final Router router) {
        this.name = name;
        init(router);
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse resp) throws Exception {
        final var html = """
                <html>
                    <meta charset="UTF-8">
                    <title>%s | DEV PAGE </title>
                    Api:<br>
                    <ol>
                %s
                    </ol>
                    File:<br>
                    <ol>
                %s
                    </ol>
                </html>
                    """;

        resp.html(html.formatted(name, apiList, fileList));
    }

    private void init(final Router router) {
        this.apiList = of(router.handlers())
                .skip(1)
                .map(h -> {
                    var s = """
                                    <li><div>%s<a href="%s">%s</a></div></li>
                            """;
                    return s.formatted("&nbsp" + padAfter(h.method().name, 10, "-") + "&nbsp", h.path(), h.path());
                })
                .collect(joining(""));

        this.fileList = of(listFileNames(""))
                .map(h -> {
                    router.GET("/" + h, (req, response) -> {
                        response.html(readString(h, CHARSET));
                    });
                    var s = """
                                    <li><div><a href="/%s">%s</a></div></li>
                            """;
                    return s.formatted(h, h);
                })
                .collect(joining(""));
    }
}