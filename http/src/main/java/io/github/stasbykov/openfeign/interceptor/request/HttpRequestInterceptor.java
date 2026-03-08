package io.github.stasbykov.openfeign.interceptor.request;

import feign.RequestTemplate;

/**
 * Request interceptor abstraction independent of Feign's native interface.
 */
public interface HttpRequestInterceptor {
    /**
     * Applies request modifications.
     *
     * @param template request template
     */
    void apply(RequestTemplate template);
}
