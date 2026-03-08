package io.github.stasbykov.openfeign.service.authorization.token;

import java.time.Instant;

/**
 * Base token implementation with expiration support.
 */
public abstract class ExpirableToken implements AuthToken{
    private final Instant expiresAt;

    /**
     * Creates token with expiration instant.
     *
     * @param expiresAt expiration instant, may be {@code null}
     */
    protected ExpirableToken(Instant expiresAt){
        this.expiresAt = expiresAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExpired() {
        if(expiresAt == null)
            return false;
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * @return expiration instant, may be {@code null}
     */
    public Instant expiresAt() {
        return expiresAt;
    }
}
