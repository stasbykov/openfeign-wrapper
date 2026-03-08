package io.github.stasbykov.openfeign.service.authorization.cache;

import io.github.stasbykov.openfeign.service.authorization.credential.CredentialsKey;
import io.github.stasbykov.openfeign.service.authorization.token.AuthToken;

import java.util.Optional;

/**
 * Token cache abstraction keyed by credentials.
 *
 * @param <K> credentials key type
 * @param <T> auth token type
 */
public interface TokenCache<K extends CredentialsKey, T extends AuthToken> {
    /**
     * Returns cached token by key.
     *
     * @param key cache key
     * @return optional cached token
     */
    Optional<T> get(K key);

    /**
     * Stores token under key.
     *
     * @param key cache key
     * @param value token value
     */
    void put(K key, T value);

    /**
     * Clears token for key.
     *
     * @param key cache key
     */
    void clear(K key);

    /**
     * Clears all cached tokens.
     */
    void clearAll();
}
