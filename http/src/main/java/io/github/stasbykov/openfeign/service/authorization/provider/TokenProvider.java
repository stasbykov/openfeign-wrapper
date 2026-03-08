package io.github.stasbykov.openfeign.service.authorization.provider;

import io.github.stasbykov.openfeign.service.authorization.credential.Credentials;
import io.github.stasbykov.openfeign.service.authorization.token.AuthToken;

/**
 * Token provider abstraction for authorization and refresh operations.
 *
 * @param <C> credentials type
 * @param <T> token type
 */
public interface TokenProvider<C extends Credentials, T extends AuthToken> {

    /**
     * Authorizes and returns new token.
     *
     * @param credentials credentials payload
     * @return auth token
     */
    T authorize(C credentials);

    /**
     * Refreshes token.
     *
     * @param oldToken current token
     * @param credentials credentials payload
     * @return refreshed token
     */
    T refresh(T oldToken, C credentials);
}
