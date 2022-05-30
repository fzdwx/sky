package core.http;

import io.github.fzdwx.lambada.anno.Nullable;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import util.Netty;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/05/29 09:03:28
 */
public class Headers extends HttpHeaders {

    private final HttpHeaders headers;

    public Headers(HttpHeaders headers) {
        this.headers = headers;
    }


    @Nullable
    public String getFirst(String key) {
        return this.headers.get(key);
    }

    public void add(String key, @Nullable String value) {
        if (value != null) {
            this.headers.add(key, value);
        }
    }

    public void addAll(String key, List<? extends String> values) {
        this.headers.add(key, values);
    }

    public void addAll(MultiValueMap<String, String> values) {
        values.forEach(this.headers::add);
    }

    public void set(String key, @Nullable String value) {
        if (value != null) {
            this.headers.set(key, value);
        }
    }

    public void setAll(Map<String, String> values) {
        values.forEach(this.headers::set);
    }

    public Map<String, String> toSingleValueMap() {
        Map<String, String> singleValueMap = CollectionUtils.newLinkedHashMap(this.headers.size());
        this.headers.entries()
                .forEach(entry -> {
                    if (!singleValueMap.containsKey(entry.getKey())) {
                        singleValueMap.put(entry.getKey(), entry.getValue());
                    }
                });
        return singleValueMap;
    }

    @Override
    public String get(final String name) {
        return headers.get(name);
    }

    @Override
    public Integer getInt(final CharSequence name) {
        return headers.getInt(name);
    }

    @Override
    public int getInt(final CharSequence name, final int defaultValue) {
        return headers.getInt(name, defaultValue);
    }

    @Override
    public Short getShort(final CharSequence name) {
        return headers.getShort(name);
    }

    @Override
    public short getShort(final CharSequence name, final short defaultValue) {
        return headers.getShort(name, defaultValue);
    }

    @Override
    public Long getTimeMillis(final CharSequence name) {
        return headers.getTimeMillis(name);
    }

    @Override
    public long getTimeMillis(final CharSequence name, final long defaultValue) {
        return headers.getTimeMillis(name, defaultValue);
    }

    @Override
    public List<String> getAll(final String name) {
        return headers.getAll(name);
    }

    @Override
    public List<Map.Entry<String, String>> entries() {
        return headers.entries();
    }

    @Override
    public boolean contains(final String name) {
        return headers.contains(name);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return headers.iterator();
    }

    @Override
    public Iterator<Map.Entry<CharSequence, CharSequence>> iteratorCharSequence() {
        return headers.iteratorCharSequence();
    }

    @Override
    public boolean isEmpty() {
        return this.headers.isEmpty();
    }

    @Override
    public int size() {
        return this.headers.names().size();
    }

    @Override
    public Set<String> names() {
        return headers.names();
    }

    @Override
    public HttpHeaders add(final String name, final Object value) {
        return headers.add(name, value);
    }

    @Override
    public HttpHeaders add(final String name, final Iterable<?> values) {
        return headers.add(name, values);
    }

    @Override
    public HttpHeaders addInt(final CharSequence name, final int value) {
        return headers.addInt(name, value);
    }

    @Override
    public HttpHeaders addShort(final CharSequence name, final short value) {
        return headers.addShort(name, value);
    }

    @Override
    public HttpHeaders set(final String name, final Object value) {
        return headers.set(name, value);
    }

    @Override
    public HttpHeaders set(final String name, final Iterable<?> values) {
        return headers.set(name, values);
    }

    @Override
    public HttpHeaders setInt(final CharSequence name, final int value) {
        return headers.setInt(name, value);
    }

    @Override
    public HttpHeaders setShort(final CharSequence name, final short value) {
        return headers.setShort(name, value);
    }

    @Override
    public HttpHeaders remove(final String name) {
        return headers.remove(name);
    }

    @Override
    public Headers clear() {
        this.headers.clear();
        return this;
    }

    @Override
    public String toString() {
        return Netty.formatHeaders(this);
    }

    public boolean containsKey(Object key) {
        return (key instanceof String && this.headers.contains((String) key));
    }

    public boolean containsValue(Object value) {
        return (value instanceof String &&
                this.headers.entries().stream()
                        .anyMatch(entry -> value.equals(entry.getValue())));
    }

    @Nullable
    public List<String> get(Object key) {
        if (containsKey(key)) {
            return this.headers.getAll((String) key);
        }
        return null;
    }

    @Nullable
    public List<String> put(String key, @Nullable List<String> value) {
        List<String> previousValues = this.headers.getAll(key);
        this.headers.set(key, value);
        return previousValues;
    }

    @Nullable
    public List<String> remove(Object key) {
        if (key instanceof String) {
            List<String> previousValues = this.headers.getAll((String) key);
            this.headers.remove((String) key);
            return previousValues;
        }
        return null;
    }

    public void putAll(Map<? extends String, ? extends List<String>> map) {
        map.forEach(this.headers::set);
    }

    public Set<String> keySet() {
        return new HeaderNames();
    }

    public Collection<List<String>> values() {
        return this.headers.names().stream()
                .map(this.headers::getAll).collect(Collectors.toList());
    }

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return new AbstractSet<Map.Entry<String, List<String>>>() {
            @Override
            public Iterator<Map.Entry<String, List<String>>> iterator() {
                return new EntryIterator();
            }

            @Override
            public int size() {
                return headers.size();
            }
        };
    }

    private class EntryIterator implements Iterator<Map.Entry<String, List<String>>> {

        private Iterator<String> names = headers.names().iterator();

        @Override
        public boolean hasNext() {
            return this.names.hasNext();
        }

        @Override
        public Map.Entry<String, List<String>> next() {
            return new HeaderEntry(this.names.next());
        }
    }


    private class HeaderEntry implements Map.Entry<String, List<String>> {

        private final String key;

        HeaderEntry(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public List<String> getValue() {
            return headers.getAll(this.key);
        }

        @Override
        public List<String> setValue(List<String> value) {
            List<String> previousValues = headers.getAll(this.key);
            headers.set(this.key, value);
            return previousValues;
        }
    }

    private class HeaderNames extends AbstractSet<String> {

        @Override
        public Iterator<String> iterator() {
            return new HeaderNamesIterator(headers.names().iterator());
        }

        @Override
        public int size() {
            return headers.names().size();
        }
    }

    private final class HeaderNamesIterator implements Iterator<String> {

        private final Iterator<String> iterator;

        @Nullable
        private String currentName;

        private HeaderNamesIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public String next() {
            this.currentName = this.iterator.next();
            return this.currentName;
        }

        @Override
        public void remove() {
            if (this.currentName == null) {
                throw new IllegalStateException("No current Header in iterator");
            }
            if (!headers.contains(this.currentName)) {
                throw new IllegalStateException("Header not present: " + this.currentName);
            }
            headers.remove(this.currentName);
        }
    }

}