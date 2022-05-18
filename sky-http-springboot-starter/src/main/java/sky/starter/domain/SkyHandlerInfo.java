package sky.starter.domain;

import io.github.fzdwx.lambada.http.Router;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import sky.starter.ext.SkyHttpMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/17 22:05
 */
public class SkyHandlerInfo {

    private static final SortedSet<PathPattern> EMPTY_PATH_PATTERN = new TreeSet<>(Collections.singleton(new PathPatternParser().parse("")));
    private String name;
    private SortedSet<PathPattern> patterns;
    private Set<RequestMethod> methods;
    private SortedSet<String> headers;
    private SortedSet<String> consumer;
    private SortedSet<String> producer;
    private ResponseBody json = null;

    public SkyHandlerInfo(final PathPatternParser patternParser, final String... path) {
        this(parse(patternParser, path));
    }

    private SkyHandlerInfo(final SortedSet<PathPattern> patterns) {
        this.patterns = patterns;
    }


    public SkyHandlerInfo(final ResponseBody json, final String name, final Collection<PathPattern> pathPatterns,
                          final Collection<RequestMethod> methods,
                          final Collection<String> headers, final Collection<String> consumer, final Collection<String> producer) {
        this.name = name;
        this.json = json;
        this.patterns = new TreeSet<>(pathPatterns);
        this.methods = new TreeSet<>(methods);
        this.headers = new TreeSet<>(headers);
        this.consumer = new TreeSet<>(consumer);
        this.producer = new TreeSet<>(producer);
    }

    public static SkyHandlerInfo paths(final PathPatternParser patternParser, final String... path) {
        return new SkyHandlerInfo(patternParser, path);
    }

    public Set<PathPattern> paths() {
        return patterns;
    }

    public Boolean enableJson() {
        return json != null;
    }

    public ResponseBody responseBody() {
        return json;
    }

    public SkyHandlerInfo methods(final RequestMethod... method) {
        this.methods = (ObjectUtils.isEmpty(method) ? Collections.emptySet() : new LinkedHashSet<>(Arrays.asList(method)));
        return this;
    }

    public SkyHandlerInfo headers(final String... headers) {
        this.headers = new TreeSet<>(Arrays.asList(headers));
        return this;
    }

    public SkyHandlerInfo consumer(final String... consumes) {
        this.consumer = new TreeSet<>(Arrays.asList(consumes));
        return this;
    }

    public SkyHandlerInfo producer(final String... produces) {
        this.producer = new TreeSet<>(Arrays.asList(produces));
        return this;
    }

    public SkyHandlerInfo name(final String name) {
        this.name = name;
        return this;
    }

    public SkyHandlerInfo combine(final SkyHandlerInfo other) {
        final var name = combineNames(other);
        final var pathPatterns = combinePatterns(other.patterns);
        final var methods = combineMethods(other.methods);
        final var headers = combineHeaders(other.headers);
        final var consumer = combineConsumer(other.consumer);
        final var producer = combineProducer(other.producer);
        final var json = combineJson(other.json);
        return new SkyHandlerInfo(json, name, pathPatterns, methods, headers, consumer, producer);
    }

    public void addToRouter(final Router<SkyRouteDefinition> router, final SkyHttpMethod handlerMethod) {
        patterns.forEach(pattern -> {
            for (final RequestMethod method : this.methods) {
                router.addRoute(method.name(), pattern.getPatternString(), new SkyRouteDefinition(this, handlerMethod));
            }
        });
    }

    public SkyHandlerInfo json(final ResponseBody anno) {
        if (this.json == null) {
            this.json = anno;
        }
        return this;
    }

    private ResponseBody combineJson(final ResponseBody other) {
        if (this.json != null) {
            return this.json;
        }

        return other;
    }

    private Set<String> combineProducer(final SortedSet<String> other) {
        if (other == null || other.isEmpty()) {
            return this.producer;
        }
        if (this.producer == null || this.producer.isEmpty()) {
            return other;
        }

        Set<String> set = new LinkedHashSet<>(this.producer);
        set.addAll(other);
        return set;
    }

    private Set<String> combineConsumer(final SortedSet<String> other) {
        if (other == null || other.isEmpty()) {
            return this.consumer;
        }
        if (this.consumer == null || this.consumer.isEmpty()) {
            return other;
        }

        Set<String> set = new LinkedHashSet<>(this.consumer);
        set.addAll(other);
        return set;
    }

    private Set<String> combineHeaders(final SortedSet<String> other) {
        if (other == null || other.isEmpty()) {
            return this.headers;
        }
        if (this.headers == null || this.headers.isEmpty()) {
            return other;
        }

        Set<String> set = new LinkedHashSet<>(this.headers);
        set.addAll(other);
        return set;
    }

    private Set<RequestMethod> combineMethods(final Set<RequestMethod> other) {
        if (other == null || other.isEmpty()) {
            return this.methods;
        }
        if (this.methods == null || this.methods.isEmpty()) {
            return other;
        }

        Set<RequestMethod> set = new LinkedHashSet<>(this.methods);
        set.addAll(other);
        return set;
    }

    private SortedSet<PathPattern> combinePatterns(final SortedSet<PathPattern> other) {
        if (other == null || other.isEmpty()) {
            return this.patterns;
        }
        if (this.patterns == null || this.patterns.isEmpty()) {
            return other;
        }

        SortedSet<PathPattern> combined = new TreeSet<>();
        for (PathPattern pattern1 : this.patterns) {
            for (PathPattern pattern2 : other) {
                combined.add(pattern1.combine(pattern2));
            }
        }
        return combined;
    }

    private String combineNames(final SkyHandlerInfo other) {
        if (this.name != null && other.name != null) {
            String separator = RequestMappingInfoHandlerMethodMappingNamingStrategy.SEPARATOR;
            return this.name + separator + other.name;
        } else if (this.name != null) {
            return this.name;
        } else {
            return other.name;
        }
    }

    private static SortedSet<PathPattern> parse(PathPatternParser parser, String... patterns) {
        if (patterns.length == 0 || (patterns.length == 1 && !StringUtils.hasText(patterns[0]))) {
            return EMPTY_PATH_PATTERN;
        }
        SortedSet<PathPattern> result = new TreeSet<>();
        for (String path : patterns) {
            if (StringUtils.hasText(path) && !path.startsWith("/")) {
                path = "/" + path;
            }
            result.add(parser.parse(path));
        }
        return result;
    }

}