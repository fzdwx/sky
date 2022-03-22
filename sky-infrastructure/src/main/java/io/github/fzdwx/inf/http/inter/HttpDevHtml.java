package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.http.core.ContentType;
import io.github.fzdwx.inf.http.core.HttpHandler;
import io.github.fzdwx.inf.http.core.HttpServerRequest;
import io.github.fzdwx.inf.http.core.HttpServerResponse;
import io.github.fzdwx.inf.route.Router;
import lombok.extern.slf4j.Slf4j;

import static cn.hutool.core.io.FileUtil.listFileNames;
import static cn.hutool.core.text.CharSequenceUtil.padAfter;
import static io.github.fzdwx.lambada.Seq.of;
import static java.util.stream.Collectors.joining;

/**
 * dev html.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 21:47
 * @since 0.06
 */
@Slf4j
public class HttpDevHtml implements HttpHandler {

    private final String name;
    private String apiList;
    private String fileList;
    public static final String PAGE_PATH = "/dev";

    public HttpDevHtml(final String name, final Router router) {
        this.name = name;
        init(router);
    }

    @Override
    public void handle(final HttpServerRequest request, final HttpServerResponse resp) throws Exception {
        final var html = """
                <html lang="zh">
                    <meta charset="UTF-8">
                    <title>%s | DEV PAGE </title>
                    <img src="/favicon.ico" width="100" height="100"  alt="favicon" style='display: block;margin: 0 auto'>
                    <div id='app' style='position: fixed; left: 0;top: 0'>
                        <span>Api:</span>
                        <ol>
                %s
                        </ol>
                        <span>File:</span>
                        <ol>
                %s
                        </ol>
                    </div>
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

        // mount file path to router.
        this.fileList = of(listFileNames(""))
                .map(h -> {
                    router.GET("/" + h, (req, response) -> {
                        response.contentType(ContentType.TEXT_PLAIN)
                                .mountBodyEnd(f -> {
                                    System.out.println("body end~");
                                })
                                .file(h);
                    });
                    var s = """
                                        <li><div><a href="/%s">%s</a></div></li>
                            """;
                    return s.formatted(h, h);
                })
                .collect(joining(""));
    }
}