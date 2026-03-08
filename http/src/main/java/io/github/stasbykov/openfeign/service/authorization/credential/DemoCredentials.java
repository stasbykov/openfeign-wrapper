package io.github.stasbykov.openfeign.service.authorization.credential;

import static io.github.stasbykov.StringUtil.isPresent;

/**
 * Credentials payload used for demo auth service authorization.
 */
public record DemoCredentials(String grantType, String clientId, String secretKey) implements Credentials, CredentialsKey {

    /**
     * {@inheritDoc}
     */
    @Override
    public String cacheKey() {
        return clientId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate() {
        return isPresent(grantType) && isPresent(clientId) && isPresent(secretKey);
    }
}
