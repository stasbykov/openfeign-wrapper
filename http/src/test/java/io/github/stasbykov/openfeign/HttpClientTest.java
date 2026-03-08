package io.github.stasbykov.openfeign;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import feign.RequestLine;
import io.github.stasbykov.openfeign.retrier.RetryPolicy;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Properties;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static io.github.stasbykov.json.extractor.JacksonExtractor.getStringOrNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for {@link HttpClient} using WireMock.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("HttpClient behavior across different client and service configurations")
public class HttpClientTest {

    private WireMockServer wireMockServer;
    private static final String HOST = "http://localhost";
    private static final String TOKEN = "Bearer token";
    private static final String X_API_KEY = "API-KEY";

    /**
     * Test API contract used by Feign client proxy.
     */
    interface TestService {
        @RequestLine("GET /test")
        RawHttpResponse sendTest();

    }

    //
    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(Options.DYNAMIC_PORT);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        clearSystemProperties();
    }

    @Test
    @DisplayName("Sends request with HttpClient without additional configuration")
    void shouldSuccessSendRequestWithoutAdditionalSettings() {

        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());

        stubFor(get(urlEqualTo("/test"))
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)));

        TestService testService = HttpClient.builder().fromServiceConfig("test-service").build(TestService.class);
        RawHttpResponse response = testService.sendTest();

        verify(1, getRequestedFor(urlEqualTo("/test")));

        int actualStatus = response.status();
        String actualFieldStatus = getStringOrNull(response.body(), "/status");

        assertEquals(200, actualStatus);
        assertEquals("success", actualFieldStatus);
    }

    @Test
    @DisplayName("Throws error when service base URL is empty")
    void shouldThrowExceptionWhenBaseUrlIsEmpty() {
        System.setProperty("test.service.base.url", "");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> HttpClient.builder().fromServiceConfig("test-service").build(TestService.class));

        assertTrue(exception.getMessage().contains("baseUrl must not be null or blank"));
    }

    @Test
    @DisplayName("Sends request with authorization token from configuration")
    void shouldSuccessSendRequestWithAuthorizationTokenFromConfig() {
        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());
        System.setProperty("test.service.token", TOKEN);

        stubFor(get(urlEqualTo("/test"))
                .withHeader("Authorization", equalTo(TOKEN))
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)));

        TestService testService = HttpClient.builder().fromServiceConfig("test-service").build(TestService.class);
        testService.sendTest();

        verify(1, getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", equalTo(TOKEN)));
    }

    @Test
    @DisplayName("Sends request with X-API-Key from configuration")
    void shouldSuccessSendRequestWithXApiKeyFromConfig() {
        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());
        System.setProperty("test.service.api.key", X_API_KEY);

        stubFor(get(urlEqualTo("/test"))
                .withHeader("X-API-Key", equalTo(X_API_KEY))
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)));

        TestService testService = HttpClient.builder().fromServiceConfig("test-service").build(TestService.class);
        testService.sendTest();

        verify(1, getRequestedFor(urlEqualTo("/test"))
                .withHeader("X-API-Key", equalTo(X_API_KEY)));
    }

    @ParameterizedTest
    @DisplayName("Throws error for invalid connectTimeout and readTimeout values")
    @ValueSource(strings = {
            "test.service.connect.timeout: ",
            "test.service.connect.timeout:abc",
            "test.service.read.timeout: ",
            "test.service.read.timeout:abc"})
    void shouldThrowExceptionWhenConnectTimeoutIsIncorrect(String incorrectTimeout) {
        String[] parseParam = incorrectTimeout.split(":");

        String paramName = parseParam[0];
        String incorrectValue = parseParam[1];

        System.out.println(paramName + " : " + incorrectValue);

        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());
        System.setProperty(paramName, incorrectValue);

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> HttpClient.builder().fromServiceConfig("test-service").build(TestService.class));

        assertEquals("Cannot convert '" + incorrectValue + "' to int", exception.getMessage());

    }

    @ParameterizedTest
    @DisplayName("Throws error for negative connectTimeout and readTimeout values")
    @ValueSource(strings = {"test.service.connect.timeout", "test.service.read.timeout"})
    void shouldThrowExceptionWhenConnectTimeoutIsNegative(String propertyName) {
        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());
        System.setProperty(propertyName, "-1");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> HttpClient.builder().fromServiceConfig("test-service").build(TestService.class));

        assertEquals("Timeout must not be null or negative\n" +
                "Current value: -1" , exception.getMessage());
    }

    @Test
    @DisplayName("Retries request sending on network timeout errors")
    void shouldRetrySendRequests() {
        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());
        System.setProperty("test.service.read.timeout", "100");

        stubFor(get(urlEqualTo("/test"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("ATTEMPT_2")
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)));

        stubFor(get(urlEqualTo("/test"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("ATTEMPT_1")
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)
                        .withFixedDelay(200))
                .willSetStateTo("ATTEMPT_2"));;

        stubFor(get(urlEqualTo("/test"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("Started")
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)
                        .withFixedDelay(200))
                .willSetStateTo("ATTEMPT_1"));;


        TestService testService = HttpClient.builder()
                .fromServiceConfig("test-service")
                .retryPolicy(RetryPolicy.fixedRetry(5, Duration.ofMillis(100)))
                .build(TestService.class);
        RawHttpResponse response = testService.sendTest();

        verify(3, getRequestedFor(urlEqualTo("/test")));

        int actualStatus = response.status();
        String actualFieldStatus = getStringOrNull(response.body(), "/status");

        assertEquals(200, actualStatus);
        assertEquals("success", actualFieldStatus);
    }

    @Test
    @DisplayName("Adds custom headers")
    void shouldSuccessAddedHeader() {
        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());

        stubFor(get(urlEqualTo("/test"))
                .withHeader("Authorization", equalTo(TOKEN))
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)));

        TestService testService = HttpClient.builder()
                .fromServiceConfig("test-service")
                .requestInterceptor(template -> template.header("Authorization", TOKEN))
                .build(TestService.class);

        testService.sendTest();

        verify(1, getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", equalTo(TOKEN)));
    }

    @Test
    @DisplayName("Ignores null values in custom header arguments")
    void shouldIgnoreNullHeader() {
        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());

        stubFor(get(urlEqualTo("/test"))
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(200)));

        TestService testService = HttpClient.builder()
                .fromServiceConfig("test-service")
                .requestInterceptors()
                .build(TestService.class);

        testService.sendTest();

        verify(1, getRequestedFor(urlEqualTo("/test")));
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 303, 400, 401,403, 409, 500, 503, 504 })
    @DisplayName("Processes different response status codes correctly")
    void shouldCorrectProcessingDifferentResponseStatus(int statusCode) {
        System.setProperty("test.service.base.url", HOST + ":" + wireMockServer.port());

        stubFor(get(urlEqualTo("/test"))
                .willReturn(okJson("{\"status\": \"success\"}")
                        .withStatus(statusCode)));

        TestService testService = HttpClient.builder()
                .fromServiceConfig("test-service")
                .build(TestService.class);

        RawHttpResponse response = testService.sendTest();

        verify(1, getRequestedFor(urlEqualTo("/test")));

        int actualStatus = response.status();
        String actualFieldStatus = getStringOrNull(response.body(), "/status");

        assertEquals(statusCode, actualStatus);
        assertEquals("success", actualFieldStatus);
    }

    private void clearSystemProperties() {
        Properties properties = System.getProperties();
        properties.remove("test.service.base.url");
        properties.remove("test.service.connect.timeout");
        properties.remove("test.service.read.timeout");
        properties.remove("test.service.token");
        properties.remove("test.service.api.key");
    }
}
