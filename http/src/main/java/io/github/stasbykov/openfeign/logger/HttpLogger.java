package io.github.stasbykov.openfeign.logger;

import feign.Logger;

/**
 * Base abstraction for selecting Feign logger implementation and log level.
 */
public abstract class HttpLogger {

    private final LogLevel level;

    /**
     * Creates logger abstraction with selected level.
     *
     * @param level desired log level
     */
    public HttpLogger(LogLevel level) {
        this.level = level;
    }

    /**
     * @return concrete Feign logger instance
     */
    abstract public Logger toLogger();

    /**
     * @return mapped Feign log level
     */
    public final Logger.Level toLevel() {
        return switch (level) {
            case BASIC -> Logger.Level.BASIC;
            case FULL -> Logger.Level.FULL;
            case NONE -> Logger.Level.NONE;
            case HEADERS -> Logger.Level.HEADERS;
            default -> throw new IllegalStateException("Unknown log level " + level);
        };
    }

    /**
     * Supported logging levels.
     */
    public enum LogLevel {
        BASIC,
        FULL,
        NONE,
        HEADERS

    }
}
