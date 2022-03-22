package io.github.fzdwx.inf.core;

/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/3/22 21:56
 */
@FunctionalInterface
public interface Disposable {

    /**
     * Cancel or dispose the underlying task or resource.
     * <p>
     * Implementations are required to make this method idempotent.
     */
    void dispose();

    /**
     * Optionally return {@literal true} when the resource or task is disposed.
     * <p>
     * Implementations are not required to track disposition and as such may never
     * return {@literal true} even when disposed. However, they MUST only return true
     * when there's a guarantee the resource or task is disposed.
     *
     * @return {@literal true} when there's a guarantee the resource or task is disposed.
     */
    default boolean isDisposed() {
        return false;
    }
}