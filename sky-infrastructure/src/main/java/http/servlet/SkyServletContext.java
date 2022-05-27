package http.servlet;

import io.github.fzdwx.lambada.http.ContentType;
import io.github.fzdwx.lambada.http.HttpPath;
import io.github.fzdwx.lambada.lang.StringPool;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

/**
 * impl ServletContext.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/27 17:13
 */
@Slf4j
public class SkyServletContext implements ServletContext {

    private ClassLoader classLoader = getClass().getClassLoader();
    private String contextPath = StringPool.EMPTY;

    public SkyServletContext() {
    }

    /**
     * set classLoader
     */
    public SkyServletContext withClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * set contextPath
     */
    public SkyServletContext withContextPath(String contextPath) {
        this.contextPath = HttpPath.format(contextPath);
        return this;
    }


    @Override
    public String getContextPath() {
        return this.contextPath;
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
    public String getMimeType(final String file) {
        return ContentType.parseFromFileName(file);
    }

    @Override
    public Set<String> getResourcePaths(final String path) {
        return null;
    }

    @Override
    public URL getResource(final String path) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(final String path) {
        return null;
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