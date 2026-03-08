package io.github.stasbykov.openfeign.logger;

import feign.Logger;

/**
 * SLF4J-based logger adapter for Feign.
 */
public final class Slf4jLogger extends HttpLogger {

    /**
     * Creates SLF4J logger adapter.
     *
     * @param level desired log level
     */
    public Slf4jLogger(LogLevel level) {
        super(level);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger toLogger() {
        return new feign.slf4j.Slf4jLogger();
    }

}
