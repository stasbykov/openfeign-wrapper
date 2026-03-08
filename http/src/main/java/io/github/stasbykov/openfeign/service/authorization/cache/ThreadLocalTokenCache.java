package io.github.stasbykov.openfeign.service.authorization.cache;

import io.github.stasbykov.openfeign.service.authorization.credential.CredentialsKey;
import io.github.stasbykov.openfeign.service.authorization.token.AuthToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Thread-local token cache implementation.
 *
 * @param <K> credentials key type
 * @param <T> auth token type
 */
public abstract  class ThreadLocalTokenCache<K extends CredentialsKey, T extends AuthToken> implements TokenCache<K, T> {

    private final ThreadLocal<Map<String, T>> storage = ThreadLocal.withInitial(HashMap::new);

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<T> get(K key) {
        return Optional.ofNullable(storage.get().get(key.cacheKey()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(K key, T value) {
        storage.get().put(key.cacheKey(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear(K key) {
        storage.get().remove(key.cacheKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAll() {
        storage.get().clear();
    }
}
