package io.github.stasbykov.owner;

import org.aeonbits.owner.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ServiceConfigFactory}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceConfigFactoryTest {

    /**
     * Test Owner configuration contract.
     */
    @Config.Sources({
            "system:env",
            "system:properties"
    })
    @Config.LoadPolicy(Config.LoadType.MERGE)
    interface TestConfig extends Config {
        @Key("${service}.field.1")
        @DefaultValue("default_field1")
        String field1();

        @Key("${service}.field.2")
        @DefaultValue("default_field2")
        String field2();

        @Key("${service}.field.3")
        @DefaultValue("default_field3")
        String field3();

        @Key("${service}.field.4")
        @DefaultValue("1")
        Integer field4();
    }

    @Test
    void shouldSuccessCreateDefaultConfig() {

        TestConfig config = ServiceConfigFactory.create(TestConfig.class, "test-service");

        assertAll(
                () -> assertEquals("default_field1", config.field1()),
                () -> assertEquals("default_field2", config.field2()),
                () -> assertEquals("default_field3", config.field3()),
                () -> assertEquals(1, config.field4())
        );
    }


    @Test
    void shouldSuccessOverridingConfig() {

        Map<String, String> overrides = Map.of(
                "field.1", "custom_field1",
                "field.2", "custom_field2",
                "field.3", "custom_field3",
                "field.4", "2"
        );

        TestConfig config = ServiceConfigFactory.create(TestConfig.class, "test-service", overrides);

        assertAll(
                () -> assertEquals("custom_field1", config.field1()),
                () -> assertEquals("custom_field2", config.field2()),
                () -> assertEquals("custom_field3", config.field3()),
                () -> assertEquals(2, config.field4())
        );
    }

    @Test
    void shouldIgnoreIncorrectKeysOverrideConfig() {

        Map<String, String> overrides = Map.of(
                "test.service.field.1", "custom_field1",
                "incorrect", "custom_field2",
                "field.3", "custom_field3"
        );

        TestConfig config = ServiceConfigFactory.create(TestConfig.class, "test-service", overrides);

        assertAll(
                () -> assertEquals("default_field1", config.field1()),
                () -> assertEquals("default_field2", config.field2()),
                () -> assertEquals("custom_field3", config.field3()),
                () -> assertEquals(1, config.field4())
        );
    }

    @Test
    void shouldSuccessCreateConfigWithSystemProperties() {
        System.setProperty("test.service.field.1", "custom_field1");
        System.setProperty("test.service.field.2", "custom_field2");
        System.setProperty("test.service.field.3", "custom_field3");
        System.setProperty("test.service.field.4", "2");

        TestConfig config = ServiceConfigFactory.create(TestConfig.class, "test-service");

        assertAll(
                () -> assertEquals("custom_field1", config.field1()),
                () -> assertEquals("custom_field2", config.field2()),
                () -> assertEquals("custom_field3", config.field3()),
                () -> assertEquals(2, config.field4())
        );

        System.clearProperty("test.service.field.1");
        System.clearProperty("test.service.field.2");
        System.clearProperty("test.service.field.3");
        System.clearProperty("test.service.field.4");

    }

    @Test
    void shouldRejectNullClassType() {

        NullPointerException thrown = assertThrows(NullPointerException.class, () -> ServiceConfigFactory.create(null, "test-service", Map.of()));
        String expectedMessage = "Class type must not be null";
        assertTrue(
                thrown.getMessage().contains(expectedMessage),
                String.format(
                        "Unexpected error message.%n" +
                                "Expected message to contain: %s%n" +
                                "Actual message: %s",
                        expectedMessage,
                        thrown.getMessage()
                )
        );
    }

    @Test
    void shouldRejectNullOverrideProps() {
        NullPointerException thrown = assertThrows(NullPointerException.class, () -> ServiceConfigFactory.create(TestConfig.class, "test-service", null));
        String expectedMessage = "Overrides must not be null";
        assertTrue(
                thrown.getMessage().contains(expectedMessage),
                String.format(
                        "Unexpected error message.%n" +
                                "Expected message to contain: %s%n" +
                                "Actual message: %s",
                        expectedMessage,
                        thrown.getMessage()
                )
        );
    }

    @Test
    void shouldRejectNullServiceName() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ServiceConfigFactory.create(TestConfig.class, null, Map.of()));
        String expectedMessage = "Service name must not be null or blank";
        assertTrue(
                thrown.getMessage().contains(expectedMessage),
                String.format(
                        "Unexpected error message.%n" +
                                "Expected message to contain: %s%n" +
                                "Actual message: %s",
                        expectedMessage,
                        thrown.getMessage()
                )
        );
    }

    @Test
    void shouldRejectEmptyServiceName() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> ServiceConfigFactory.create(TestConfig.class, ""));
        String expectedMessage = "Service name must not be null or blank";
        assertTrue(
                thrown.getMessage().contains(expectedMessage),
                String.format(
                        "Unexpected error message.%n" +
                                "Expected message to contain: %s%n" +
                                "Actual message: %s",
                        expectedMessage,
                        thrown.getMessage()
                )
        );
    }

    @Test
    void shouldRejectInvalidServiceKeys() {

        Map<String, String> overrides = Map.of(
                "", "custom_field1",
                "  ", "custom_field2"
        );


        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                ServiceConfigFactory.create(TestConfig.class, "test-service", overrides));

        String expectedMessage = "Invalid override property keys detected";

        assertTrue(
                thrown.getMessage().contains(expectedMessage),
                String.format(
                        "Unexpected error message.%n" +
                                "Expected message to contain: %s%n" +
                                "Actual message: %s",
                        expectedMessage,
                        thrown.getMessage()
                )
        );
    }
}
