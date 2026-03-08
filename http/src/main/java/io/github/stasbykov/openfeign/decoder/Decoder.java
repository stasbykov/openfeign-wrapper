package io.github.stasbykov.openfeign.decoder;

/**
 * Adapter contract that exposes a Feign decoder.
 */
public interface Decoder {
    /**
     * @return configured Feign decoder instance
     */
    feign.codec.Decoder toDecoder();
}
