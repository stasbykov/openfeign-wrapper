package io.github.stasbykov.openfeign.interceptor.response;

import feign.InvocationContext;
import feign.ResponseInterceptor;

/**
 * Response interceptor abstraction independent from Feign's native interface.
 */
public interface HttpResponseInterceptor {
    /**
     * Intercepts response flow.
     *
     * @param invocationContext invocation context
     * @param chain interceptor chain
     * @return intercepted result
     * @throws Exception if interception fails
     */
    Object intercept(InvocationContext invocationContext, ResponseInterceptor.Chain chain) throws Exception;
}
