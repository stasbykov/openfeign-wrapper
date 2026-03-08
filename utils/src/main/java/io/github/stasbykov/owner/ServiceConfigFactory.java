package io.github.stasbykov.owner;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static io.github.stasbykov.StringUtil.isPresent;

/**
 * Factory for creating service-specific Owner configurations.
 * <p>
 * Normalized service name is used as the property prefix (hyphens, underscores and spaces are replaced with dots).
 * For example, service name {@code user-service} is normalized to {@code user.service}.
 * <p>
 * Property overrides passed through {@code overrides} have higher priority than external configuration.
 * Keys in {@code overrides} are specified without the service prefix.
 */
public final class ServiceConfigFactory {

    private static final String SERVICE_KEY = "service";
    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9.]+$");

    private static final String ERROR_NULL_CLASS = "Class type must not be null. Provide configuration interface type.";
    private static final String ERROR_NULL_OVERRIDES = "Overrides must not be null. Provide a Map<String, String> with property overrides.";
    private static final String ERROR_INVALID_SERVICE_NAME = "Service name must not be null or blank. Provide a valid service name.";

    private ServiceConfigFactory() {}

    /**
     * Creates configuration for service without property overrides.
     *
     * @param <T> configuration type
     * @param clazz configuration interface type extending {@link Config}
     * @param serviceName service name, for example {@code user-service}
     * @return initialized configuration instance
     * @throws NullPointerException when {@code clazz} is {@code null}
     * @throws IllegalArgumentException when {@code serviceName} is invalid
     */
    public static <T extends Config> T create(
            @NotNull Class<T> clazz,
            @NotNull String serviceName) {

        requireNonNull(clazz, ERROR_NULL_CLASS);
        validateServiceName(serviceName);

        String normalizedServiceName = normalize(serviceName);
        return ConfigFactory.create(clazz, Map.of(SERVICE_KEY, normalizedServiceName));
    }

    /**
     * Creates configuration for service with property overrides.
     *
     * @param <T> configuration type
     * @param clazz configuration interface type extending {@link Config}
     * @param serviceName service name, for example {@code user-service}
     * @param overrides property overrides without service prefix
     * @return initialized configuration instance
     * @throws NullPointerException when {@code clazz} or {@code overrides} is {@code null}
     * @throws IllegalArgumentException when {@code serviceName} is invalid or overrides contain invalid keys
     */
    public static <T extends Config> T create(
            @NotNull Class<T> clazz,
            @NotNull String serviceName,
            @NotNull Map<String, String> overrides) {

        requireNonNull(clazz, ERROR_NULL_CLASS);
        validateServiceName(serviceName);
        requireNonNull(overrides, ERROR_NULL_OVERRIDES);

        String normalizedServiceName = normalize(serviceName);
        validateOverrides(overrides);

        Map<String, String> props = appendPrefixToKeys(normalizedServiceName, overrides);
        props.put(SERVICE_KEY, normalizedServiceName);

        return ConfigFactory.create(clazz, props);
    }

    /**
     * Normalizes service name for property prefix usage.
     *
     * @param serviceName source service name
     * @return normalized service name
     * @throws NullPointerException when service name is {@code null}
     */
    private static String normalize(@NotNull String serviceName) {
        requireNonNull(serviceName, ERROR_INVALID_SERVICE_NAME);

        return serviceName.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '.')
                .replace('_', '.')
                .replace(' ', '.');
    }

    /**
     * Validates service name.
     *
     * @param serviceName service name
     * @throws IllegalArgumentException when service name is invalid
     */
    private static void validateServiceName(String serviceName) {
        if (!isPresent(serviceName)) {
            throw new IllegalArgumentException(ERROR_INVALID_SERVICE_NAME);
        }
    }

    /**
     * Validates keys in overrides map.
     *
     * @throws IllegalArgumentException when invalid keys are found
     */
    private static void validateOverrides(@NotNull Map<String, String> overrides) {
        if (overrides.isEmpty()) {
            return;
        }

        List<String> invalidKeys = overrides.keySet().stream()
                .filter(key -> !SERVICE_KEY.equals(key))
                .filter(key -> !isPresent(key) || !VALID_KEY_PATTERN.matcher(key).matches())
                .toList();

        if (!invalidKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid override property keys detected: " + invalidKeys
            );
        }
    }

    /**
     * Adds service prefix to override keys except reserved {@code service} key.
     *
     * @param prefix service prefix
     * @param overrides source overrides
     * @return prefixed overrides map
     */
    private static Map<String, String> appendPrefixToKeys(
            @NotNull String prefix,
            @NotNull Map<String, String> overrides) {

        requireNonNull(prefix, "Failed to prefix configuration keys: prefix must not be null");
        requireNonNull(overrides, "Failed to prefix configuration keys: overrides must not be null");

        if (overrides.isEmpty()) {
            return Map.of();
        }

        return overrides.entrySet().stream()
                .filter(entry -> isPresent(entry.getKey()))
                .filter(entry -> !SERVICE_KEY.equals(entry.getKey()))
                .collect(Collectors.toMap(
                        entry -> normalize(prefix) + "." + entry.getKey(),
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        HashMap::new
                ));
    }
}
