package io.github.stasbykov.openfeign.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.github.stasbykov.openfeign.RawHttpResponse;

/**
 * Feign client for obtaining OAuth2 tokens from the demo auth service.
 */
public interface DemoAuthClient {

    /**
     * Requests an access token using client credentials.
     *
     * @param grantType OAuth2 grant type
     * @param clientId client identifier
     * @param clientSecret client secret
     * @return raw auth response
     */
    @RequestLine("POST /oauth2/token")
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "cache-control: no-cache"
    })
    RawHttpResponse authorization(@Param("grant_type") String grantType,
                                  @Param("client_id") String clientId,
                                  @Param("client_secret") String clientSecret);

}

