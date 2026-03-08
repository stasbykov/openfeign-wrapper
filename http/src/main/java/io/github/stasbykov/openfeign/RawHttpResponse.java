package io.github.stasbykov.openfeign;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Raw HTTP response model returned by low-level clients.
 *
 * @param status HTTP status code
 * @param headers HTTP response headers
 * @param body response body as string
 */
public record RawHttpResponse(int status, Map<String, List<String>> headers, String body) {

    /**
     * Finds the first header value by header name (case-insensitive).
     *
     * @param name header name
     * @return optional first header value
     */
    public Optional<String> firstHeader(String name) {
        return headers.entrySet()
                .stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(name))
                .findFirst()
                .flatMap(e -> e.getValue().stream().findFirst());
    }

    /**
     * Returns a debug-friendly response representation.
     *
     * @return string representation
     */
    @Override
    public @NotNull String toString() {
        return "RawHttpResponse [status=" + status + ", headers='" + headers + "', body='" + body + "'";
    }
}
