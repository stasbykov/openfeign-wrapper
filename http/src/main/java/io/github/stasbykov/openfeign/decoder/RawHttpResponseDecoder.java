package io.github.stasbykov.openfeign.decoder;

import feign.Response;
import feign.Util;
import io.github.stasbykov.openfeign.RawHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Decoder that maps any response to {@link RawHttpResponse}.
 */
public class RawHttpResponseDecoder implements Decoder {
    /**
     * {@inheritDoc}
     */
    @Override
    public feign.codec.Decoder toDecoder() {
        return (response, type) -> {
            if (!RawHttpResponse.class.equals(type)) {
                throw new IllegalArgumentException("Unsupported return type: " + type);
            }

            int status = extractStatusCode(response);
            Map<String, List<String>> headers = extractHeaders(response);
            String body = extractBody(response);

            return new RawHttpResponse(
                    status, headers, body);
        };
    }

    private int extractStatusCode(Response response) {
        if(response == null) {
            return -1;
        }

        return response.status();
    }

    private String extractBody(Response response) throws IOException {
        if (response == null || response.body() == null) {
            return null;
        }
        return Util.toString(response.body().asReader(StandardCharsets.UTF_8));
    }

    private Map<String, List<String>> extractHeaders(Response response) {
        if (response == null || response.headers() == null) {
            return Map.of();
        }
        return response.headers()
                .entrySet()
                .stream()
                .collect(Collectors
                        .toMap(Map.Entry::getKey,
                                entry -> new ArrayList<>(entry.getValue())));
    }
}
