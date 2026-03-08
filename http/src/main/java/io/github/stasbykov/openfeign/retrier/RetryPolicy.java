package io.github.stasbykov.openfeign.retrier;

import java.time.Duration;

/**
 * Retry strategy definition for transient request failures.
 */
public interface RetryPolicy {
    /**
     * @return maximum number of retry attempts
     */
    int maxAttempts();
    /**
     * @return delay between retry attempts
     */
    Duration delay();

    /**
     * @return policy that disables retries
     */
    static RetryPolicy noRetry() {
        return new DefaultRetryPolicy(0, Duration.ZERO);
    }

    /**
     * Creates fixed-delay retry policy.
     *
     * @param maxAttempts maximum number of retry attempts
     * @param delay delay between attempts
     * @return retry policy
     */
    static RetryPolicy fixedRetry(int maxAttempts, Duration delay) {
        return new DefaultRetryPolicy(maxAttempts, delay);
    }
}

record DefaultRetryPolicy(int maxAttempts, Duration delay) implements RetryPolicy {}
