package io.github.stasbykov;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility methods for working with {@link String} values.
 */
public final class StringUtil {

    /**
     * Utility class constructor.
     */
    private StringUtil() {}

    /**
     * Parses a comma-separated key-value string into a map.
     *
     * @param raw source string
     * @param valueMapper value mapper function
     * @param <V> mapped value type
     * @return parsed map
     */
    public static <V> Map<String, V> parseMap(String raw, Function<String, V> valueMapper) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }

        return Arrays.stream(raw.split(","))
                .filter(entry -> !entry.trim().isBlank())
                .map(entry -> entry.split("=", 2))
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> {
                            String valueStr = (parts.length > 1) ? parts[1].trim() : "";
                            return valueMapper.apply(valueStr);
                        },
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Parses a map from string and returns default map when result is empty.
     *
     * @param raw source string
     * @param valueMapper value mapper function
     * @param defaultValue map to return when parsed result is empty
     * @param <V> mapped value type
     * @return parsed map or default map
     */
    public static <V> Map<String, V> parseMapOrDefault(String raw, Function<String, V> valueMapper, Map<String, V> defaultValue) {
        Map<String, V> result = parseMap(raw, valueMapper);
        return result.isEmpty() ? defaultValue : result;
    }

    /**
     * Checks that string is not {@code null} and not blank.
     *
     * @param value string value
     * @return {@code true} when value is present
     */
    public static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Checks that a string corresponds to a valid enum constant.
     *
     * @param value string value
     * @param enumType enum class
     * @param <T> enum type
     * @return {@code true} when enum constant exists
     */
    public static <T extends Enum<T>> boolean isPresent(String value, Class<T> enumType) {

        if(value == null || value.isBlank()) {
            return false;
        }

        try {
            Enum.valueOf(enumType, value);
            return true;
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }



    /**
     * Executes action when string is present.
     *
     * @param value string value
     * @param action action to execute
     */
    public static void ifPresent(String value, Consumer<String> action) {
        if (isPresent(value)) {
            action.accept(value);
        }
    }

    /**
     * Validates that string is present using a custom message.
     *
     * @param value string value
     * @param message error message
     * @return validated string
     * @throws IllegalArgumentException when value is {@code null} or blank
     */
    public static String requireNonNullOrEmpty(String value, String message) {
        if(!isPresent(value)) {
            throw new IllegalArgumentException(message + "\nCurrent value: " + value);
        }
        return value;
    }

    /**
     * Validates that string is present using default message.
     *
     * @param value string value
     * @return validated string
     * @throws IllegalArgumentException when value is {@code null} or blank
     */
    public static String requireNonNullOrEmpty(String value) {
        if(!isPresent(value)) {
            throw new IllegalArgumentException("String must not be null or blank. Current value: " + value);
        }
        return value;
    }

    /**
     * Executes action and returns result when string is present.
     *
     * @param value string value
     * @param action action to execute
     * @param <R> action result type
     *
     * @return optional action result or empty optional when string is not present
     */
    public static <R> Optional<R> mapIfPresent(String value, Function<String, R> action) {
        if (isPresent(value)) {
            return Optional.ofNullable(action.apply(value));
        }
        return Optional.empty();
    }

}
