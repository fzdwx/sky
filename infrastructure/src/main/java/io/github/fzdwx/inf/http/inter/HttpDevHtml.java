package io.github.fzdwx.inf.http.inter;

import io.github.fzdwx.inf.Handler;
import io.github.fzdwx.inf.http.HttpRequest;
import io.github.fzdwx.inf.http.HttpResponse;
import io.github.fzdwx.inf.route.Router;
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

    public HttpDevHtml(final Router router) {
        this.router = router;
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse resp) throws Exception {
        final var apiList = Seq.of(router.handlers())
                .skip(1)
                .map(h -> "<a href='" + h.path() + "'> " + h.method().name + " --- " + h.path() + " </a><br>")
                .collect(Collectors.joining(""));
        final var html = """
                                 <meta charset="UTF-8">
                                 <title>fzdwx dev page</title> 
                                 """ + apiList;

        resp.html(html);
    }
}