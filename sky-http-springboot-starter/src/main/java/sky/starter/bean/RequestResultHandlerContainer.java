package sky.starter.bean;

import sky.starter.ext.RequestResultHandler;
import sky.starter.ext.SomeContainer;

import java.util.Collection;
import java.util.TreeSet;

/**
 * is RequestResultHandler container.
 * <p>
 * you can inject this Container  to add your own RequestResultHandler.
 *
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/19 21:52
 */
public class RequestResultHandlerContainer extends SomeContainer<RequestResultHandler> {

    public RequestResultHandlerContainer() {
        super(new TreeSet<>());
    }

    @Override
    public RequestResultHandlerContainer add(final RequestResultHandler requestResultHandler) {
        super.add(requestResultHandler);
        return this;
    }

    @Override
    public RequestResultHandlerContainer add(final RequestResultHandler... some) {
        super.add(some);
        return this;
    }


    @Override
    public RequestResultHandlerContainer addAll(final Collection<RequestResultHandler> requestResultHandlers) {
        super.addAll(requestResultHandlers);
        return this;
    }

    @Override
    public RequestResultHandlerContainer impl() {
        return this;
    }
}