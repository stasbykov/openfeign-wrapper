package io.github.stasbykov.openfeign.interceptor.request;

import feign.RequestTemplate;
import io.github.stasbykov.openfeign.HttpClient;
import io.github.stasbykov.openfeign.client.DemoAuthClient;
import io.github.stasbykov.openfeign.encoder.FormUrlEncoder;
import io.github.stasbykov.openfeign.retrier.RetryPolicy;
import io.github.stasbykov.openfeign.service.authorization.TokenHandler;
import io.github.stasbykov.openfeign.service.authorization.cache.DemoThreadLocalTokenCache;
import io.github.stasbykov.openfeign.service.authorization.credential.DemoCredentials;
import io.github.stasbykov.openfeign.service.authorization.provider.DemoTokenProvider;
import io.github.stasbykov.openfeign.service.authorization.token.DemoToken;

import java.time.Duration;

import static io.github.stasbykov.openfeign.constants.Services.DEMO_AUTH_SERVICE;

/**
 * Request interceptor that adds `Authorization: Bearer ...` header using demo auth service.
 */
public final class DemoAuthRequestInterceptor implements HttpRequestInterceptor {

    private final DemoCredentials demoCredentials;

    /**
     * Creates interceptor with static client credentials.
     *
     * @param grantType OAuth2 grant type
     * @param clientId client identifier
     * @param clientSecret client secret
     */
    public DemoAuthRequestInterceptor(String grantType, String clientId, String clientSecret) {
        demoCredentials = new DemoCredentials(grantType, clientId, clientSecret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(RequestTemplate template) {
        DemoAuthClient demoAuthClient = HttpClient.builder()
                .fromServiceConfig(DEMO_AUTH_SERVICE)
                .encoder(new FormUrlEncoder())
                .retryPolicy(RetryPolicy.fixedRetry(5, Duration.ofSeconds(1)))
                .build(DemoAuthClient.class);
        DemoTokenProvider provider = new DemoTokenProvider(demoAuthClient);
        DemoThreadLocalTokenCache cache = new DemoThreadLocalTokenCache();
        TokenHandler<DemoCredentials, DemoToken> tokenHandler = new TokenHandler<>(provider, cache);

        String token = tokenHandler.getTokenValue(demoCredentials);

        template.header("Authorization", "Bearer " + token);
    }
}
