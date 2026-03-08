package io.github.stasbykov.openfeign.interceptor.response;

import feign.InvocationContext;
import feign.ResponseInterceptor;

/**
 * Adapter that bridges {@link HttpResponseInterceptor} to Feign {@link ResponseInterceptor}.
 */
public class FeignResponseInterceptor implements ResponseInterceptor {

    private final HttpResponseInterceptor delegate;

    /**
     * Creates adapter instance.
     *
     * @param delegate delegate interceptor
     */
    public FeignResponseInterceptor(HttpResponseInterceptor delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        return delegate.intercept(invocationContext, chain);
    }
}
