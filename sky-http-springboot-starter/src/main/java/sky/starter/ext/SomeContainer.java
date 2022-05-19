package sky.starter.ext;

import io.github.fzdwx.lambada.Collections;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/19 22:01
 */
public abstract class SomeContainer<Some>{

    Collection<Some> container;

    protected SomeContainer(final Collection<Some> container) {
        this.container = container;
    }

    public SomeContainer<Some> add(Some some) {
        if (some != null) {
            container.add(some);
        }
        return impl();
    }

    public SomeContainer<Some> add(Some... some) {
        if (some != null) {
            container.addAll(Arrays.asList(some));
        }
        return impl();
    }

    public SomeContainer<Some> addAll(Collection<Some> somes) {
        if (somes != null) {
            this.container.addAll(somes);
        }
        return impl();
    }

    public abstract SomeContainer<Some> impl();

    /**
     * unmodifiable.
     *
     * @return {@link Collection }<{@link Some }>
     */
    public final Collection<Some> container() {
        return Collections.unmodifiable(container);
    }
}