package io.github.stasbykov.openfeign.interceptor;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import feign.RequestLine;
import io.github.stasbykov.openfeign.HttpClient;
import io.github.stasbykov.openfeign.interceptor.request.DemoAuthRequestInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration-style tests for {@link DemoAuthRequestInterceptor}.
 */
@ExtendWith(WireMockExtension.class)
@DisplayName("Demo auth interceptor behavior")
public class DemoAuthInterceptorTest {

    private WireMockServer demoAuthService;
    private WireMockServer testService;
    private static final String HOST = "http://localhost";
    private static final String AUTH_TOKEN = "test-auth-token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";

    @BeforeEach
    void setUp() {
        demoAuthService = startWireMockServer();
        testService = startWireMockServer();
    }

    @AfterEach
    void tearDown() {
        stopWireMockServer(demoAuthService);
        stopWireMockServer(testService);
    }

    private WireMockServer startWireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(Options.DYNAMIC_PORT);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        return wireMockServer;
    }

    private void stopWireMockServer(WireMockServer wireMockServer) {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("Adds authorization token to request header for valid client credentials")
    void shouldSuccessInsertTokenWithValidCredentialsToRequestHeader() {
        setupServices(demoAuthService, testService);
        stubdemoAuthServiceForValidToken(demoAuthService);
        stubTestServiceForSuccessfulTest(testService, AUTH_TOKEN);

        TestClient testClient = createTestClient(GRANT_TYPE, CLIENT_ID, CLIENT_SECRET);
        testClient.test();

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", equalTo("Bearer " + AUTH_TOKEN)));
    }

    @Test
    @DisplayName("Fails token injection for invalid client credentials")
    void shouldFailWhenInvalidCredentials() {
        setupServices(demoAuthService, testService);
        stubdemoAuthServiceForInvalidCredentials(demoAuthService);

        TestClient testClient = createTestClient(GRANT_TYPE, "wrong_client_id", CLIENT_SECRET);

        Exception exception = assertThrows(Exception.class, testClient::test);

        assertTrue(exception.getMessage().contains("Receipt token error. Response is null or status code is not 200"));
        demoAuthService.verify(1, postRequestedFor(urlEqualTo("/oauth2/token")));
    }

    @Test
    @DisplayName("""
            Retries after auth service timeout and then successfully receives token
            and executes request to target test service
            """)
    void shouldRetryRequestAfterTimeout() {
        setupServices(demoAuthService, testService);
        System.setProperty("demo.auth.service.read.timeout", "100");
        stubdemoAuthServiceForTimeoutAndRetry(demoAuthService, AUTH_TOKEN);
        stubTestServiceForSuccessfulTest(testService, AUTH_TOKEN);

        TestClient testClient = createTestClient(GRANT_TYPE, CLIENT_ID, CLIENT_SECRET);
        testClient.test();

        testService.verify(1, getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", equalTo("Bearer " + AUTH_TOKEN)));
        demoAuthService.verify(3, postRequestedFor(urlEqualTo("/oauth2/token")));
    }

    private void setupServices(WireMockServer demoAuthService, WireMockServer testService) {
        System.setProperty("test.service.base.url", DemoAuthInterceptorTest.HOST + ":" + testService.port());
        System.setProperty("demo.auth.service.base.url", DemoAuthInterceptorTest.HOST + ":" + demoAuthService.port());
    }

    private void stubdemoAuthServiceForValidToken(WireMockServer demoAuthService) {
        demoAuthService.stubFor(post(urlEqualTo("/oauth2/token"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
                .withHeader("cache-control", equalTo("no-cache"))
                .withFormParam("grant_type", equalTo(GRANT_TYPE))
                .withFormParam("client_id", equalTo(CLIENT_ID))
                .withFormParam("client_secret", equalTo(CLIENT_SECRET))
                .willReturn(okJson("{\"access_token\": \"" + AUTH_TOKEN + "\", \"refresh_token\": \"" + AUTH_TOKEN + "\", \"expires_at\": \"" + Instant.now().plus(Duration.ofDays(1)) + "\"}")));
    }

    private void stubdemoAuthServiceForInvalidCredentials(WireMockServer demoAuthService) {
        demoAuthService.stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("{\"error\": \"invalid_client\"}")));
    }

    private void stubdemoAuthServiceForTimeoutAndRetry(WireMockServer demoAuthService, String token) {
        String newTokenResponse = "{\"access_token\": \"" + token + "\", \"refresh_token\": \"" + token + "\", \"expires_at\": \"" + Instant.now().plus(Duration.ofDays(1)) + "\"}";

        demoAuthService.stubFor(post(urlEqualTo("/oauth2/token"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("ATTEMPT_2")
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
                .withHeader("cache-control", equalTo("no-cache"))
                .withFormParam("grant_type", equalTo(GRANT_TYPE))
                .withFormParam("client_id", equalTo(CLIENT_ID))
                .withFormParam("client_secret", equalTo(CLIENT_SECRET))
                .willReturn(okJson(newTokenResponse)));

        demoAuthService.stubFor(post(urlEqualTo("/oauth2/token"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("ATTEMPT_1")
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
                .withHeader("cache-control", equalTo("no-cache"))
                .withFormParam("grant_type", equalTo(GRANT_TYPE))
                .withFormParam("client_id", equalTo(CLIENT_ID))
                .withFormParam("client_secret", equalTo(CLIENT_SECRET))
                .willReturn(okJson(newTokenResponse)
                        .withFixedDelay(200))
                .willSetStateTo("ATTEMPT_2"));

        demoAuthService.stubFor(post(urlEqualTo("/oauth2/token"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("Started")
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
                .withHeader("cache-control", equalTo("no-cache"))
                .withFormParam("grant_type", equalTo(GRANT_TYPE))
                .withFormParam("client_id", equalTo(CLIENT_ID))
                .withFormParam("client_secret", equalTo(CLIENT_SECRET))
                .willReturn(okJson(newTokenResponse)
                        .withFixedDelay(200))
                .willSetStateTo("ATTEMPT_1"));
    }

    private void stubTestServiceForSuccessfulTest(WireMockServer testService, String token) {
        testService.stubFor(get(urlEqualTo("/test"))
                .withHeader("Authorization", equalTo("Bearer " + token))
                .willReturn(okJson("{\"status\": \"success\"}")));
    }

    private TestClient createTestClient(String grantType, String clientId, String clientSecret) {
        return HttpClient.builder()
                .fromServiceConfig("test-service")
                .requestInterceptor(new DemoAuthRequestInterceptor(grantType, clientId, clientSecret))
                .build(TestClient.class);
    }

    /**
     * Test API contract used by Feign client proxy.
     */
    private interface TestClient {
        @RequestLine("GET /test")
        void test();
    }
}
