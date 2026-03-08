package io.github.stasbykov.openfeign;

import feign.Feign;
import feign.Request;
import feign.Retryer;
import io.github.stasbykov.openfeign.config.ServiceConfig;
import io.github.stasbykov.openfeign.decoder.Decoder;
import io.github.stasbykov.openfeign.decoder.RawHttpResponseDecoder;
import io.github.stasbykov.openfeign.encoder.Encoder;
import io.github.stasbykov.openfeign.encoder.JacksonEncoder;
import io.github.stasbykov.openfeign.interceptor.request.FeignRequestInterceptor;
import io.github.stasbykov.openfeign.interceptor.request.HttpRequestInterceptor;
import io.github.stasbykov.openfeign.interceptor.response.FeignResponseInterceptor;
import io.github.stasbykov.openfeign.interceptor.response.HttpResponseInterceptor;
import io.github.stasbykov.openfeign.interceptor.response.RawHttpResponseInterceptor;
import io.github.stasbykov.openfeign.logger.HttpLogger;
import io.github.stasbykov.openfeign.logger.Slf4jLogger;
import io.github.stasbykov.openfeign.retrier.RetryPolicy;
import io.github.stasbykov.owner.ServiceConfigFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.github.stasbykov.DurationUtil.requireNonNullOrNegative;
import static io.github.stasbykov.StringUtil.ifPresent;
import static io.github.stasbykov.StringUtil.requireNonNullOrEmpty;
import static io.github.stasbykov.openfeign.constants.Configuration.DEFAULT_CONNECT_TIMEOUT;
import static io.github.stasbykov.openfeign.constants.Configuration.DEFAULT_READ_TIMEOUT;
import static java.util.Objects.requireNonNull;

/**
 * Entry point for building strongly typed OpenFeign clients.
 */
public final class HttpClient {

    private HttpClient() {}

    /**
     * Creates a new HTTP client builder.
     *
     * @return a new builder instance
     */
    public static HttpClientBuilder builder() {
        return new HttpClientBuilder();
    }

    /**
     * Fluent builder for creating OpenFeign clients with shared configuration defaults.
     */
    public static final class HttpClientBuilder {

        private static final String BASEURL_ERROR_MESS = "baseUrl must not be null or blank";
        private static final String TIMEOUT_ERROR_MESS = "Timeout must not be null or negative";
        private static final String SERIALIZABLE_ERROR_MESS = "Encoder must not be null";
        private static final String DESERIALIZABLE_ERROR_MESS = "Decoder must not be null";
        private static final String RETRY_POLICY_ERROR_MESS = "Retry policy must not be null";
        private static final String INTERCEPTOR_ERROR_MESS = "Interceptor must not be null";
        private static final String HTTP_LOGGER_ERROR_MESS = "Logger must not be null";

        private String baseUrl;
        private Duration connectTimeout = Duration.ofMillis(Long.parseLong(DEFAULT_CONNECT_TIMEOUT));
        private Duration readTimeout = Duration.ofMillis(Long.parseLong(DEFAULT_READ_TIMEOUT));
        private Encoder encoder = new JacksonEncoder();
        private Decoder decoder = new RawHttpResponseDecoder();
        private HttpLogger httpLogger = new Slf4jLogger(HttpLogger.LogLevel.BASIC);
        private RetryPolicy retryPolicy = RetryPolicy.noRetry();
        private final List<HttpRequestInterceptor> requestInterceptors = new ArrayList<>();
        private final List<HttpResponseInterceptor> responseInterceptors = new ArrayList<>(List.of(new RawHttpResponseInterceptor()));

        private HttpClientBuilder() {}

        /**
         * Loads base URL, auth, API key and timeout settings from {@link ServiceConfig}.
         *
         * @param serviceName service identifier used by {@link ServiceConfigFactory}
         * @return this builder
         */
        public HttpClientBuilder fromServiceConfig(String serviceName) {
            ServiceConfig config = requireNonNull(ServiceConfigFactory.create(ServiceConfig.class, serviceName), "Failed to load service configuration");

            ifPresent(config.baseUrl(), this::baseUrl);
            ifPresent(config.apiKey(), apiKey -> addHeaderInterceptor("X-API-Key", apiKey));
            ifPresent(config.token(), token -> addHeaderInterceptor("Authorization", token));
            this.connectTimeout = Duration.ofMillis(config.connectTimeout());
            this.readTimeout = Duration.ofMillis(config.readTimeout());

            return this;
        }

        /**
         * Sets target base URL.
         *
         * @param baseUrl target service base URL
         * @return this builder
         */
        public HttpClientBuilder baseUrl(String baseUrl) {
            this.baseUrl = requireNonNullOrEmpty(baseUrl, BASEURL_ERROR_MESS);
            return this;
        }

        /**
         * Sets connection timeout.
         *
         * @param connectTimeout timeout duration
         * @return this builder
         */
        public HttpClientBuilder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = requireNonNullOrNegative(connectTimeout, TIMEOUT_ERROR_MESS);
            return this;
        }

        /**
         * Sets read timeout.
         *
         * @param readTimeout timeout duration
         * @return this builder
         */
        public HttpClientBuilder readTimeout(Duration readTimeout) {
            this.readTimeout = requireNonNullOrNegative(readTimeout, TIMEOUT_ERROR_MESS);
            return this;
        }

        /**
         * Sets request encoder.
         *
         * @param encoder custom encoder
         * @return this builder
         */
        public HttpClientBuilder encoder(Encoder encoder) {
            this.encoder = requireNonNull(encoder, SERIALIZABLE_ERROR_MESS);
            return this;
        }

        /**
         * Sets response decoder.
         *
         * @param decoder custom decoder
         * @return this builder
         */
        public HttpClientBuilder decoder(Decoder decoder) {
            this.decoder = requireNonNull(decoder, DESERIALIZABLE_ERROR_MESS);
            return this;
        }

        /**
         * Sets HTTP logger implementation.
         *
         * @param httpLogger logger implementation
         * @return this builder
         */
        public HttpClientBuilder httpLogger(HttpLogger httpLogger) {
            this.httpLogger = requireNonNull(httpLogger, HTTP_LOGGER_ERROR_MESS);
            return this;
        }

        /**
         * Sets retry policy.
         *
         * @param retryPolicy retry policy
         * @return this builder
         */
        public HttpClientBuilder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = requireNonNull(retryPolicy, RETRY_POLICY_ERROR_MESS);
            return this;
        }

        /**
         * Adds request interceptors. Null list is ignored, null items are skipped.
         *
         * @param interceptors request interceptors
         * @return this builder
         */
        public HttpClientBuilder requestInterceptors(List<HttpRequestInterceptor> interceptors) {
            if (interceptors == null) {
                return this;
            }
            interceptors.stream()
                    .filter(Objects::nonNull)
                    .forEach(this::requestInterceptor);
            return this;
        }

        /**
         * Adds request interceptors passed as varargs.
         *
         * @param interceptors request interceptors
         * @return this builder
         */
        public HttpClientBuilder requestInterceptors(HttpRequestInterceptor... interceptors) {
            return requestInterceptors(interceptors == null ? null : Arrays.asList(interceptors));
        }

        /**
         * Adds a single request interceptor.
         *
         * @param interceptor request interceptor
         * @return this builder
         */
        public HttpClientBuilder requestInterceptor(HttpRequestInterceptor interceptor) {
            this.requestInterceptors.add(requireNonNull(interceptor, INTERCEPTOR_ERROR_MESS));
            return this;
        }

        /**
         * Adds response interceptors. Null list is ignored, null items are skipped.
         *
         * @param interceptors response interceptors
         * @return this builder
         */
        public HttpClientBuilder responseInterceptors(List<HttpResponseInterceptor> interceptors) {
            if (interceptors == null) {
                return this;
            }
            interceptors.stream()
                    .filter(Objects::nonNull)
                    .forEach(this::responseInterceptor);
            return this;
        }

        /**
         * Adds response interceptors passed as varargs.
         *
         * @param interceptors response interceptors
         * @return this builder
         */
        public HttpClientBuilder responseInterceptors(HttpResponseInterceptor... interceptors) {
            return responseInterceptors(interceptors == null ? null : Arrays.asList(interceptors));
        }

        /**
         * Adds a single response interceptor.
         *
         * @param interceptor response interceptor
         * @return this builder
         */
        public HttpClientBuilder responseInterceptor(HttpResponseInterceptor interceptor) {
            this.responseInterceptors.add(requireNonNull(interceptor, INTERCEPTOR_ERROR_MESS));
            return this;
        }

        /**
         * Builds and targets the Feign client.
         *
         * @param clientType interface type describing the remote API
         * @param <T> client type
         * @return proxy client instance
         */
        public <T> T build(Class<T> clientType) {
            requireNonNullOrEmpty(baseUrl, BASEURL_ERROR_MESS);
            requireNonNull(encoder, SERIALIZABLE_ERROR_MESS);
            requireNonNull(decoder, DESERIALIZABLE_ERROR_MESS);
            requireNonNullOrNegative(connectTimeout, TIMEOUT_ERROR_MESS);
            requireNonNullOrNegative(readTimeout, TIMEOUT_ERROR_MESS);
            requireNonNull(retryPolicy, RETRY_POLICY_ERROR_MESS);

            Feign.Builder builder = Feign.builder()
                    .encoder(encoder.toEncoder())
                    .decoder(decoder.toDecoder())
                    .options(new Request.Options(
                            connectTimeout.toMillis(),
                            TimeUnit.MILLISECONDS,
                            readTimeout.toMillis(),
                            TimeUnit.MILLISECONDS,
                            true))
                    .retryer(new Retryer.Default(
                            retryPolicy.delay().toMillis(),
                            retryPolicy.delay().toMillis(),
                            retryPolicy.maxAttempts()
                    ));

            if(httpLogger != null)
                builder.logger(httpLogger.toLogger());

            requestInterceptors.stream()
                    .map(FeignRequestInterceptor::new)
                    .forEach(builder::requestInterceptor);

            responseInterceptors.stream()
                    .map(FeignResponseInterceptor::new)
                    .forEach(builder::responseInterceptor);

            return builder.target(clientType, baseUrl);
        }

        private void addHeaderInterceptor(String param, String value) {
            this.requestInterceptors.add(template -> template.header(param, value));
        }
    }
}
