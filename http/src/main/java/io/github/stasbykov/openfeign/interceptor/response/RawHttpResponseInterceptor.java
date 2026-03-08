package io.github.stasbykov.openfeign.interceptor.response;

import feign.InvocationContext;
import feign.Response;
import feign.ResponseInterceptor;
import io.github.stasbykov.openfeign.RawHttpResponse;

/**
 * Interceptor that directly decodes responses for methods returning {@link RawHttpResponse}.
 */
public final class RawHttpResponseInterceptor implements HttpResponseInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object intercept(InvocationContext invocationContext, ResponseInterceptor.Chain chain) throws Exception {
        Response response = invocationContext.response();

        if(invocationContext.returnType().equals(RawHttpResponse.class)) {
            return invocationContext.decoder().decode(invocationContext.response(), invocationContext.returnType());
        }

        return chain.next(invocationContext);
    }
}
