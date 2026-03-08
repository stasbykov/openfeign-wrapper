package io.github.stasbykov.openfeign.service.authorization.token;

/**
 * Minimal token contract.
 */
public interface AuthToken {
    /**
     * @return token value used in authorization header
     */
    String value();
    /**
     * @return {@code true} if token is expired
     */
    boolean isExpired();
}
