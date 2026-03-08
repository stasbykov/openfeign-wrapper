package io.github.stasbykov.openfeign.encoder;

/**
 * Provides a Jackson-based Feign encoder.
 */
public final class JacksonEncoder implements Encoder {
    /**
     * {@inheritDoc}
     */
    @Override
    public feign.codec.Encoder toEncoder() {
        return new feign.jackson.JacksonEncoder();
    }
}
