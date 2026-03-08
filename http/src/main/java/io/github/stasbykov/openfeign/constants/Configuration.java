package io.github.stasbykov.openfeign.constants;

/**
 * Shared default configuration constants.
 */
public final class Configuration {

    private Configuration() {}

    /** Default connection timeout in milliseconds. */
    public static final String DEFAULT_CONNECT_TIMEOUT = "5000";
    /** Default read timeout in milliseconds. */
    public static final String DEFAULT_READ_TIMEOUT = "10000";
}
