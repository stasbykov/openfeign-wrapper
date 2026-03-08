package io.github.stasbykov.openfeign.decoder;

/**
 * Provides Jackson-based Feign decoder.
 */
public final class JacksonDecoder implements Decoder {

    /**
     * {@inheritDoc}
     */
    @Override
    public feign.codec.Decoder toDecoder() {
        return new feign.jackson.JacksonDecoder();
    }
}
