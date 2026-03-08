package io.github.stasbykov.openfeign.service.authorization;

import io.github.stasbykov.openfeign.service.authorization.cache.TokenCache;
import io.github.stasbykov.openfeign.service.authorization.credential.Credentials;
import io.github.stasbykov.openfeign.service.authorization.credential.CredentialsKey;
import io.github.stasbykov.openfeign.service.authorization.provider.TokenProvider;
import io.github.stasbykov.openfeign.service.authorization.token.AuthToken;

/**
 * Coordinates token retrieval, cache lookup and refresh/authorization flow.
 *
 * @param <C> credentials type
 * @param <T> token type
 */
public final class TokenHandler<C extends Credentials & CredentialsKey, T extends AuthToken> {

    private final TokenProvider<C, T> tokenProvider;
    private final TokenCache<C, T> cache;

    /**
     * Creates token handler instance.
     *
     * @param tokenProvider token provider implementation
     * @param cache token cache implementation
     */
    public TokenHandler(TokenProvider<C, T> tokenProvider, TokenCache<C, T> cache) {
        this.tokenProvider = tokenProvider;
        this.cache = cache;
    }

    /**
     * Returns a valid token from cache or provider.
     *
     * @param credentials credentials for auth request
     * @return valid token
     */
    public T getToken(C credentials) {
        return cache.get(credentials)
                .map(t -> validateOrRefresh(t, credentials))
                .orElseGet(() -> authorizedCache(credentials));

    }

    /**
     * Returns token string value.
     *
     * @param credentials credentials for auth request
     * @return token value
     */
    public String getTokenValue(C credentials) {
        return getToken(credentials).value();
    }

    private T validateOrRefresh(T token, C credentials) {
        if(!token.isExpired()) {
            return token;
        }
        T refreshedToken = tokenProvider.refresh(token, credentials);
        cache.put(credentials, refreshedToken);
        return refreshedToken;
    }

    private T authorizedCache(C credentials) {
        T newToken = tokenProvider.authorize(credentials);
        cache.put(credentials, newToken);
        return newToken;
    }
}
