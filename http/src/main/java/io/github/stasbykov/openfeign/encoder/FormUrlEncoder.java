package io.github.stasbykov.openfeign.encoder;

import feign.form.FormEncoder;

/**
 * Provides an URL-encoded form Feign encoder.
 */
public final class FormUrlEncoder implements Encoder {
    /**
     * {@inheritDoc}
     */
    @Override
    public feign.codec.Encoder toEncoder() {
        return new FormEncoder();
    }
}
