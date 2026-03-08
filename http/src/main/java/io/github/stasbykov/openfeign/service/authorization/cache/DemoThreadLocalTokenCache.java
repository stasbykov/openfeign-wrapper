package io.github.stasbykov.openfeign.service.authorization.cache;

import io.github.stasbykov.openfeign.service.authorization.credential.DemoCredentials;
import io.github.stasbykov.openfeign.service.authorization.token.DemoToken;

/**
 * Thread-local cache specialization for demo auth credentials/tokens.
 */
public class DemoThreadLocalTokenCache extends ThreadLocalTokenCache<DemoCredentials, DemoToken> {}
