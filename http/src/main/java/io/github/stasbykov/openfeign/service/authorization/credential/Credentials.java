package io.github.stasbykov.openfeign.service.authorization.credential;

/**
 * Marker contract for authorization credentials with self-validation capability.
 */
public interface Credentials {
    /**
     * Validates credentials content.
     *
     * @return {@code true} when credentials are valid
     */
    boolean validate();
}
