package io.github.stasbykov.openfeign.service.authorization.token;

import java.time.Instant;

/**
 * Token model used by demo auth service.
 */
public final class DemoToken extends ExpirableToken {

    private final String accessToken;
    private final String refreshToken;

    /**
     * Creates demo token.
     *
     * @param accessToken access token value
     * @param refreshToken refresh token value
     * @param expiresAt expiration instant
     */
    public DemoToken(String accessToken, String refreshToken, Instant expiresAt) {
        super(expiresAt);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value() {
        return accessToken;
    }

    /**
     * @return refresh token value
     */
    public String refreshToken() {
        return refreshToken;
    }
}
