package sky.starter;

import io.github.fzdwx.lambada.anno.Nullable;
import io.github.fzdwx.lambada.http.ContentType;
import io.github.fzdwx.lambada.http.UrlEncode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Set;

/**
 * did I need to implement it?
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/16 22:31
 */
@Slf4j
public class SkyServletContext implements ServletContext {

    private String name = "sky web server";
    private String path;
    private String encodedPath;

    public SkyServletContext(final String path) {
        setPath(path);
    }

    private void setPath(final String path) {
        boolean invalid = false;
        if (path == null || path.equals("/")) {
            invalid = true;
            this.path = "";
        } else if (path.isEmpty() || path.startsWith("/")) {
            this.path = path;
        } else {
            invalid = true;
            this.path = "/" + path;
        }
        if (this.path.endsWith("/")) {
            invalid = true;
            this.path = this.path.substring(0, this.path.length() - 1);
        }

        if (invalid) {
            log.warn("pathInvalid {}", this.path);
        }
        encodedPath = UrlEncode.DEFAULT.encode(this.path, StandardCharsets.UTF_8);
    }

    @Override
    public String getContextPath() {
        return path;
    }

    @Override
    public ServletContext getContext(final String uripath) {
        return this;
    }

    @Override
    public int getMajorVersion() {
        return 2;
    }

    @Override
    public int getMinorVersion() {
        return 5;
    }

    @Override
    @Nullable
    public String getMimeType(final String file) {
        return ContentType.parseFromFileName(file);
    }

    @Override
    public Set getResourcePaths(final String path) {
        return null;
    }

    @Override
    public URL getResource(final String path) throws MalformedURLException {
        try {
            return ResourceUtils.getURL(path);
        } catch (FileNotFoundException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    @Override
    public InputStream getResourceAsStream(final String path) {
        try {
            return getResource(path).openStream();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(final String name) {
        return null;
    }

    @Override
    public Servlet getServlet(final String name) throws ServletException {
        return null;
    }

    @Override
    public Enumeration getServlets() {
        return null;
    }

    @Override
    public Enumeration getServletNames() {
        return null;
    }

    @Override
    public void log(final String msg) {

    }

    @Override
    public void log(final Exception exception, final String msg) {

    }

    @Override
    public void log(final String message, final Throwable throwable) {

    }

    @Override
    public String getRealPath(final String path) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public String getInitParameter(final String name) {
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        return null;
    }

    @Override
    public Object getAttribute(final String name) {
        return null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return null;
    }

    @Override
    public void setAttribute(final String name, final Object object) {

    }

    @Override
    public void removeAttribute(final String name) {

    }

    @Override
    public String getServletContextName() {
        return null;
    }
}