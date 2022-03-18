package io.github.fzdwx.inf.http.inter;

import cn.hutool.core.io.FileUtil;
import io.github.fzdwx.inf.Handler;
import io.github.fzdwx.inf.http.HttpRequest;
import io.github.fzdwx.inf.http.HttpResponse;
import io.github.fzdwx.inf.route.Router;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.Seq;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

/**
 * dev html.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/18 21:47
 */
@Slf4j
public class HttpDevHtml implements Handler {

    private final Router router;
    private final String name;
    private String apiList;
    private String fileList;

    public HttpDevHtml(final String name, final Router router) {
        this.router = router;
        this.name = name;
        init(router);
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse resp) throws Exception {
        final var html = """
                                 <meta charset="UTF-8">
                                 <title>%s | DEV PAGE </title> 
                                 """
                         +"Api:<br>"
                         + apiList
                         +"File:<br>"
                         + fileList;
        resp.html(html.formatted(name));
    }

    private void init(final Router router) {
        this.apiList = Seq.of(router.handlers())
                .skip(1)
                .map(h -> "&nbsp;&nbsp;&nbsp;&nbsp;<a href='" + h.path() + "'> " + h.method().name + " --- " + h.path() + " </a><br>")
                .collect(Collectors.joining(""));

        this.fileList = Seq.of(FileUtil.listFileNames(""))
                .map(h -> {
                    router.GET("/" + h, (req, response) -> {
                        response.html(FileUtil.readString(h, Lang.CHARSET));
                    });
                    return "&nbsp;&nbsp;&nbsp;&nbsp;<a href='/" + h + "'> " + h + " </a><br>";
                })
                .collect(Collectors.joining(""));
    }
}