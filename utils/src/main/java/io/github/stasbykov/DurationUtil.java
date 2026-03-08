package io.github.stasbykov;

import java.time.Duration;

/**
 * Utility methods for validating {@link Duration} values.
 */
public final class DurationUtil {

    private DurationUtil() {
    }

    /**
     * Validates that duration is not {@code null} and not negative.
     *
     * @param value duration to validate
     * @return validated duration
     * @throws IllegalArgumentException when duration is {@code null} or negative
     */
    public static Duration requireNonNullOrNegative(Duration value) {
        if (value == null || value.isNegative()) {
            throw new IllegalArgumentException("""
                    Duration must not be null or negative.
                    Current value:\s""" + value);
        }
        return value;
    }

    /**
     * Validates that duration is not {@code null} and not negative using a custom message.
     *
     * @param value duration to validate
     * @param message custom validation message
     * @return validated duration
     * @throws IllegalArgumentException when duration is {@code null} or negative
     */
    public static Duration requireNonNullOrNegative(Duration value, String message) {
        if (value == null || value.isNegative()) {
            throw new IllegalArgumentException(message + "\nCurrent value: " + (value == null ? "NULL" : value.toMillis()));
        }
        return value;
    }
}
