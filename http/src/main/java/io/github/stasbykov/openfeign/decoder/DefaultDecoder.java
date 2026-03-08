package io.github.stasbykov.openfeign.decoder;

/**
 * Provides Feign default decoder.
 */
public final class DefaultDecoder implements Decoder {
    /**
     * {@inheritDoc}
     */
    @Override
    public feign.codec.Decoder toDecoder() {
        return new feign.codec.Decoder.Default();
    }
}
