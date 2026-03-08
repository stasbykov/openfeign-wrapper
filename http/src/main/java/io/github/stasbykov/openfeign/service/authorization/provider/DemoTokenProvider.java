package io.github.stasbykov.openfeign.service.authorization.provider;

import io.github.stasbykov.openfeign.RawHttpResponse;
import io.github.stasbykov.openfeign.client.DemoAuthClient;
import io.github.stasbykov.openfeign.service.authorization.credential.DemoCredentials;
import io.github.stasbykov.openfeign.service.authorization.token.DemoToken;

import java.time.Instant;
import java.util.Objects;

import static io.github.stasbykov.json.extractor.JacksonExtractor.getStringOrNull;

/**
 * Demo auth token provider that obtains tokens through {@link DemoAuthClient}.
 */
public class DemoTokenProvider implements TokenProvider<DemoCredentials, DemoToken> {

    private final DemoAuthClient demoAuthClient;

    /**
     * Creates token provider.
     *
     * @param demoAuthClient demo auth client
     */
    public DemoTokenProvider(DemoAuthClient demoAuthClient) {
        this.demoAuthClient = demoAuthClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemoToken authorize(DemoCredentials credentials) {
        credentials.validate();
        RawHttpResponse responseAuth = demoAuthClient.authorization(credentials.grantType(), credentials.clientId(), credentials.secretKey());
        return receiptToken(responseAuth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemoToken refresh(DemoToken oldToken, DemoCredentials credentials) {
        return authorize(credentials);
    }

    private DemoToken receiptToken(RawHttpResponse response) {
        if (response == null || response.status() != 200) {
            throw new RuntimeException("""
                        Receipt token error. Response is null or status code is not 200.
                        Response: %s
                        Status code: %s
                    """.formatted(
                    response == null || response.body() == null ? "undefined" : response.body(),
                    response == null ? "undefined" : response.status()
            ));
        }

        String body = response.body();
        return new DemoToken(
                getStringOrNull(body, "/access_token"),
                getStringOrNull(body, "/refresh_token"),
                Instant.parse(Objects.requireNonNull(getStringOrNull(body, "/expires_at"))));
    }
}
