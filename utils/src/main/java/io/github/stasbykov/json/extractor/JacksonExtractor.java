package io.github.stasbykov.json.extractor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Functional utility for working with JSON paths in Jackson.
 * Returns null for missing/invalid values.
 */
public final class JacksonExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\[(\\d+)]");

    /**
     * Private constructor to prevent instantiation.
     */
    private JacksonExtractor() {}

    /* =========================================================
       Public methods — JsonNode + path
       ========================================================= */

    /**
     * Extracts string value from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return string value or null when value is missing or invalid
     */
    public static String getStringOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isValueNode() ? n.asText() : null);
    }

    /**
     * Extracts integer value from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return integer value or null when value is missing or invalid
     */
    public static Integer getIntOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isInt() || n.isLong() ? n.asInt() : null);
    }

    /**
     * Extracts long value from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return long value or null when value is missing or invalid
     */
    public static Long getLongOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isNumber() ? n.asLong() : null);
    }

    /**
     * Extracts double value from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return double value or null when value is missing or invalid
     */
    public static Double getDoubleOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isNumber() ? n.asDouble() : null);
    }

    /**
     * Extracts float value from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return float value or null when value is missing or invalid
     */
    public static Float getFloatOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isNumber() ? (float) n.asDouble() : null);
    }

    /**
     * Extracts boolean value from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return boolean value or null when value is missing or invalid
     */
    public static Boolean getBooleanOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isBoolean() ? n.asBoolean() : null);
    }

    /**
     * Extracts array node from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return array node or null when value is missing or invalid
     */
    public static ArrayNode getArrayOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isArray() ? (ArrayNode) n : null);
    }

    /**
     * Extracts object node from JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return object node or null when value is missing or invalid
     */
    public static ObjectNode getObjectOrNull(JsonNode node, String path) {
        return extract(node, path, n -> n.isObject() ? (ObjectNode) n : null);
    }

    /* =========================================================
       Public methods — ObjectNode + key
       ========================================================= */

    /**
     * Extracts string value from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return string value or null when value is missing or invalid
     */
    public static String getStringOrNull(ObjectNode node, String key) {
        return node == null ? null : getStringOrNull(node.get(key), "/");
    }

    /**
     * Extracts integer value from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return integer value or null when value is missing or invalid
     */
    public static Integer getIntOrNull(ObjectNode node, String key) {
        return node == null ? null : getIntOrNull(node.get(key), "/");
    }

    /**
     * Extracts long value from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return long value or null when value is missing or invalid
     */
    public static Long getLongOrNull(ObjectNode node, String key) {
        return node == null ? null : getLongOrNull(node.get(key), "/");
    }

    /**
     * Extracts double value from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return double value or null when value is missing or invalid
     */
    public static Double getDoubleOrNull(ObjectNode node, String key) {
        return node == null ? null : getDoubleOrNull(node.get(key), "/");
    }

    /**
     * Extracts float value from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return float value or null when value is missing or invalid
     */
    public static Float getFloatOrNull(ObjectNode node, String key) {
        return node == null ? null : getFloatOrNull(node.get(key), "/");
    }

    /**
     * Extracts boolean value from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return boolean value or null when value is missing or invalid
     */
    public static Boolean getBooleanOrNull(ObjectNode node, String key) {
        return node == null ? null : getBooleanOrNull(node.get(key), "/");
    }

    /**
     * Extracts array node from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return array node or null when value is missing or invalid
     */
    public static ArrayNode getArrayOrNull(ObjectNode node, String key) {
        return node == null ? null : getArrayOrNull(node.get(key), "/");
    }

    /**
     * Extracts object node from ObjectNode by key.
     *
     * @param node root ObjectNode
     * @param key value key
     * @return object node or null when value is missing or invalid
     */
    public static ObjectNode getObjectOrNull(ObjectNode node, String key) {
        return node == null ? null : getObjectOrNull(node.get(key), "/");
    }

    /* =========================================================
       Public methods — JSON String + path
       ========================================================= */

    /**
     * Extracts string value from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return string value or null when value is missing or invalid
     */
    public static String getStringOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getStringOrNull(node, path) : null;
    }

    /**
     * Extracts integer value from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return integer value or null when value is missing or invalid
     */
    public static Integer getIntOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getIntOrNull(node, path) : null;
    }

    /**
     * Extracts long value from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return long value or null when value is missing or invalid
     */
    public static Long getLongOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getLongOrNull(node, path) : null;
    }

    /**
     * Extracts double value from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return double value or null when value is missing or invalid
     */
    public static Double getDoubleOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getDoubleOrNull(node, path) : null;
    }

    /**
     * Extracts float value from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return float value or null when value is missing or invalid
     */
    public static Float getFloatOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getFloatOrNull(node, path) : null;
    }

    /**
     * Extracts boolean value from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return boolean value or null when value is missing or invalid
     */
    public static Boolean getBooleanOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getBooleanOrNull(node, path) : null;
    }

    /**
     * Extracts array node from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return array node or null when value is missing or invalid
     */
    public static ArrayNode getArrayOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getArrayOrNull(node, path) : null;
    }

    /**
     * Extracts object node from JSON string by path.
     *
     * @param json JSON string
     * @param path path to value
     * @return object node or null when value is missing or invalid
     */
    public static ObjectNode getObjectOrNull(String json, String path) {
        JsonNode node = parse(json);
        return node != null ? getObjectOrNull(node, path) : null;
    }

    /* =========================================================
       Internal logic
       ========================================================= */

    /**
     * Extracts value from JsonNode by path using mapper.
     *
     * @param node   root JsonNode
     * @param path   path to value
     * @param mapper functional mapper from JsonNode to target type
     * @param <T>    return type
     * @return mapped value or null when value is missing or invalid
     */
    private static <T> T extract(JsonNode node, String path, NodeMapper<T> mapper) {
        JsonNode target = getByPath(node, path);
        return target != null ? mapper.map(target) : null;
    }

    /**
     * Parses JSON string into JsonNode.
     *
     * @param json JSON string
     * @return JsonNode or null when JSON string is invalid
     */
    private static JsonNode parse(String json) {
        if (json == null) return null;
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Gets JsonNode by path.
     *
     * @param node root JsonNode
     * @param path path to value
     * @return JsonNode or null when value is missing
     */
    private static JsonNode getByPath(JsonNode node, String path) {
        if (node == null || path == null || path.isBlank() || "/".equals(path)) {
            return node;
        }

        String[] tokens = path.trim().replaceAll("^/+|/+$", "").split("/"); // Splits path into tokens after trimming leading and trailing slashes

        JsonNode current = node;

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            current = descend(current, token);
            if (current == null) return null;
        }

        return current;
    }

    /**
     * Descends JsonNode by token.
     *
     * @param current current JsonNode
     * @param token   descent token
     * @return JsonNode or null when descent is not possible
     */
    private static JsonNode descend(JsonNode current, String token) {
        if (current == null) return null;

        if (current.isArray()) {
            Integer idx = parseIndex(token);
            return (idx != null && idx >= 0 && idx < current.size())
                    ? current.get(idx)
                    : null;
        }

        if (current.isObject()) {
            return descendObject(current, token);
        }

        return null;
    }

    /**
     * Descends object node by token.
     *
     * @param node  current JsonNode (object node)
     * @param token descent token
     * @return JsonNode or null when descent is not possible
     */
    private static JsonNode descendObject(JsonNode node, String token) {
        String baseKey = token.contains("[")
                ? token.substring(0, token.indexOf('['))
                : token;

        JsonNode child = node.get(baseKey);
        if (child == null) return null;

        String tail = token.substring(baseKey.length());
        if (tail.isEmpty()) return child;

        Matcher matcher = BRACKET_PATTERN.matcher(tail);
        JsonNode current = child;

        while (matcher.find()) {
            if (!current.isArray()) return null;

            int idx = Integer.parseInt(matcher.group(1));
            if (idx < 0 || idx >= current.size()) return null;

            current = current.get(idx);
        }

        return current;
    }

    /**
     * Parses index from token.
     *
     * @param token token containing index
     * @return index or null when token is invalid
     */
    private static Integer parseIndex(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            Matcher matcher = BRACKET_PATTERN.matcher(token);
            return matcher.matches()
                    ? Integer.parseInt(matcher.group(1))
                    : null;
        }
    }

    /**
     * Functional interface to map JsonNode to target type.
     *
     * @param <T> return type
     */
    @FunctionalInterface
    private interface NodeMapper<T> {
        T map(JsonNode node);
    }


}
