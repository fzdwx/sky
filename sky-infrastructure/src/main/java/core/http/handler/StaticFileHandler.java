package core.http.handler;

import core.http.ext.HttpHandler;
import core.http.ext.HttpServerRequest;
import core.http.ext.HttpServerResponse;
import io.github.fzdwx.lambada.Io;
import io.github.fzdwx.lambada.Lang;
import io.github.fzdwx.lambada.http.ContentType;
import io.github.fzdwx.lambada.http.HttpMethod;
import io.github.fzdwx.lambada.lang.StringPool;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import util.Netty;
import util.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;

/**
 * impl of {@link HttpHandler} for serving static files
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/28 23:25
 */
@Slf4j
public class StaticFileHandler implements HttpHandler {

    public static final int HTTP_CACHE_SECONDS = 60 * 60 * 24 * 365;

    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[^-\\._]?[^<>&\\\"]*");
    private final String baseDir;
    private final String workspace;

    private StaticFileHandler(final String baseDir, final String workspace) {
        this.baseDir = initBaseDir(baseDir);
        this.workspace = initWorkspace(workspace);

        log.info(Utils.PREFIX + "path :" + this.baseDir + this.workspace);
    }

    public static HttpHandler create() {
        return create(null);
    }

    public static HttpHandler create(final String workspace) {
        return create(System.getProperty("user.dir"), workspace);
    }

    public static HttpHandler create(final String baseDir, final String workspace) {
        return new StaticFileHandler(baseDir, workspace);
    }

    @Override
    public void handle(final HttpServerRequest request, final HttpServerResponse response) {
        if (!HttpMethod.GET.matches(request.methodType())) {
            response.status(HttpResponseStatus.METHOD_NOT_ALLOWED).end();
            return;
        }

        final String uri = request.uri();
        final String filePath = sanitizeUri(uri);
        System.out.println(filePath);

        //region check file status
        final File file = Io.newFile(filePath);
        if (file.isHidden() || !file.exists()) {
            response.notFound();
            return;
        }

        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                final String html = getFileListing(file, uri);
                response.html(html);
            } else {
                response.redirect(uri + StringPool.SLASH_CHAR);
            }
            return;
        }

        if (!file.isFile()) {
            response.status(FORBIDDEN).end();
            return;
        }

        if (checkModify(request, file)) {
            response.status(NOT_MODIFIED).end();
        }

        final RandomAccessFile raf = Io.toRaf(file);
        if (raf == null) {
            response.notFound();
        }
        //endregion

        prepareHeaders(response, file);
        response.sendFile(raf, Netty.DEFAULT_CHUNK_SIZE).addListener(f -> {
            System.out.println("f.cause() = " + f.cause());
        });
    }

    private void prepareHeaders(final HttpServerResponse response, final File file) {
        response.contentType(Lang.defVal(ContentType.parseFromFileName(file.getPath()), ContentType.TEXT_PLAIN.value));
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(file.lastModified())));
    }

    @SneakyThrows
    private boolean checkModify(HttpServerRequest req, final File file) {
        String ifModifiedSince = req.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            return ifModifiedSinceDateSeconds == fileLastModifiedSeconds;
        }
        return false;
    }

    private String getFileListing(final File dir, final String dirPath) {
        StringBuilder buf = new StringBuilder()
                .append("<!DOCTYPE html>\r\n")
                .append("<html><head><meta charset='utf-8' /><title>")
                .append("Listing of: ")
                .append(dirPath)
                .append("</title></head><body>\r\n")

                .append("<h3>Listing of: ")
                .append(dirPath)
                .append("</h3>\r\n")

                .append("<ul>")
                .append("<li><a href=\"../\">..</a></li>\r\n");

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isHidden() || !f.canRead()) {
                    continue;
                }

                String name = f.getName();
                if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                    continue;
                }

                buf.append("<li><a href=\"")
                        .append(name)
                        .append("\">")
                        .append(name)
                        .append("</a></li>\r\n");
            }
        }

        buf.append("</ul></body></html>\r\n");
        return buf.toString();
    }

    private String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        // Convert to absolute path.
        return getPath(uri);
    }

    private String getPath(String path) {
        if (path == null || path.isEmpty()) {
            path = File.separator;
        }
        if (path.length() > 0 && path.charAt(0) != '/') {
            path = File.separator.concat(path);
        }
        String realPath;
        if (workspace.isEmpty()) {
            realPath = baseDir.concat(path);
        } else {
            realPath = baseDir.concat(workspace).concat(path);
        }
        return realPath;
    }

    private String initBaseDir(String baseDir) {
        if (baseDir == null || baseDir.isEmpty()) {
            throw new IllegalStateException("baseDir empty");
        }
        if (baseDir.startsWith("file:") || baseDir.startsWith("FILE:")) {
            baseDir = baseDir.replace("file:", "").replace("FILE:", "");
        }
        return baseDir;
    }

    private String initWorkspace(String workspace) {
        if (workspace == null || "/".equals(workspace)) {
            workspace = "";
        }
        if (workspace.length() > 0 && workspace.charAt(0) != '/') {
            workspace = "/".concat(workspace);
        }
        return workspace;
    }
}