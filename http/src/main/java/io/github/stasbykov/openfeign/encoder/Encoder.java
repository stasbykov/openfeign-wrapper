package io.github.stasbykov.openfeign.encoder;

/**
 * Adapter contract that exposes a Feign encoder.
 */
public interface Encoder {
    /**
     * @return configured Feign encoder instance
     */
    feign.codec.Encoder toEncoder();
}
