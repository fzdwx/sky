package org.atomicode.inf.http.inter;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.atomicode.fzdwx.lambada.Lang;
import org.atomicode.fzdwx.lambada.Seq;
import org.atomicode.fzdwx.lambada.lang.MimeMapping;
import org.atomicode.inf.http.core.ContentType;
import org.atomicode.inf.http.core.HttpHandler;
import org.atomicode.inf.http.core.HttpServerRequest;
import org.atomicode.inf.http.core.HttpServerResponse;
import org.atomicode.inf.route.Router;

import static cn.hutool.core.text.CharSequenceUtil.padAfter;
import static java.util.stream.Collectors.joining;
import static org.atomicode.fzdwx.lambada.Seq.of;

/**
 * dev html.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 21:47
 * @since 0.06
 */
@Slf4j
public class HttpDevHtml implements HttpHandler {

    public static final String PAGE_PATH = "/dev";
    private final String name;
    private final String staticPath;
    private String apiList;
    private String fileList;

    public HttpDevHtml(final String name, final Router router, final String staticPath) {
        this.name = name;
        this.staticPath = staticPath;
        init(router);
    }

    public HttpDevHtml(final String name, final Router router) {
        this.name = name;
        this.staticPath = "";
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
                .map(h -> {
                    var s = """
                                        <li><div>%s<a href="%s">%s</a></div></li>
                            """;
                    return s.formatted("&nbsp" + padAfter(h.method().name, 10, "-") + "&nbsp", h.path(), h.path());
                })
                .collect(joining(""));

        // mount file path to router.
        final var files = FileUtil.loopFiles(staticPath);
        this.fileList = Seq.of(files)
                .map(file -> {
                    router.GET("/" + file.getName(), (req, response) -> {
                        response.contentType(Lang.defVal(MimeMapping.getMimeTypeForExtension(file.getName()), ContentType.TEXT_PLAIN))
                                .sendFile(file.toPath());
                    });
                    var s = """
                                        <li><div><a href="/%s">%s</a></div></li>
                            """;
                    return s.formatted(file.getName(), file.getName());
                })
                .collect(joining(""));
    }
}