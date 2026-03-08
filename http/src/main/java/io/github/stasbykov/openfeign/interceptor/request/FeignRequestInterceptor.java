package io.github.stasbykov.openfeign.interceptor.request;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Adapter that bridges {@link HttpRequestInterceptor} to Feign {@link RequestInterceptor}.
 */
public final class FeignRequestInterceptor implements RequestInterceptor {

    private final HttpRequestInterceptor delegate;

    /**
     * Creates adapter instance.
     *
     * @param delegate delegate interceptor
     */
    public FeignRequestInterceptor(HttpRequestInterceptor delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        delegate.apply(requestTemplate);
    }
}
