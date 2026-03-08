package io.github.stasbykov;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility methods for working with {@link Map} values.
 */
public final class MapUtil {

    /**
     * Utility class constructor.
     */
    private MapUtil() {}

    /**
     * Checks that map is not {@code null} and not empty.
     *
     * @param value map value
     * @return {@code true} if map is present
     */
    public static boolean isPresent(Map<?, ?> value) {
        return value != null && !value.isEmpty();
    }

    /**
     * Executes action when map is present.
     *
     * @param value map value
     * @param action action to execute
     * @param <K> map key type
     * @param <V> map value type
     */
    public static <K, V> void ifPresent(Map<K, V> value, Consumer<Map<K, V>> action) {
        if(isPresent(value)) {
            action.accept(value);
        }
    }

    /**
     * Executes action and returns result when map is present.
     *
     * @param value map value
     * @param action action to execute
     * @param <K> map key type
     * @param <V> map value type
     * @param <R> action result type
     *
     * @return optional action result or empty optional when map is not present
     */
    public static <K, V, R> Optional<R> mapIfPresent(Map<K, V> value, Function<Map<K, V>, R> action) {
        if(isPresent(value)) {
            return Optional.ofNullable(action.apply(value));
        } else
            return Optional.empty();
    }

}
