package sky.starter.bean;

import sky.starter.ext.RequestArgumentResolver;
import sky.starter.ext.SomeContainer;

import java.util.Collection;
import java.util.HashSet;

/**
 * request argument resolver container
 * <p>
 * you can inject this Container  to add your own RequestArgumentResolver.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/19 21:49
 */
public class RequestArgumentResolverContainer extends SomeContainer<RequestArgumentResolver> {

    public RequestArgumentResolverContainer() {
        super(new HashSet<>());
    }

    @Override
    public RequestArgumentResolverContainer add(final RequestArgumentResolver requestArgumentResolver) {
        super.add(requestArgumentResolver);
        return this;
    }

    @Override
    public RequestArgumentResolverContainer add(final RequestArgumentResolver... some) {
        super.add(some);
        return this;
    }


    @Override
    public RequestArgumentResolverContainer addAll(final Collection<RequestArgumentResolver> requestArgumentResolvers) {
        super.addAll(requestArgumentResolvers);
        return this;
    }

    @Override
    public RequestArgumentResolverContainer impl() {
        return this;
    }
}