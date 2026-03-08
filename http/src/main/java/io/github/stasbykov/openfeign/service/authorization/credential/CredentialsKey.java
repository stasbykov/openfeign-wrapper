package io.github.stasbykov.openfeign.service.authorization.credential;

/**
 * Contract for generating stable cache keys from credentials.
 */
public interface CredentialsKey {
    /**
     * @return cache key string
     */
    String cacheKey();
}
