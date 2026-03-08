# openfeign-wrapper

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://adoptium.net/)
[![Gradle](https://img.shields.io/badge/Gradle-9.0.0-02303A?logo=gradle)](https://gradle.org/)
[![OpenFeign](https://img.shields.io/badge/OpenFeign-13.6-blue)](https://github.com/OpenFeign/feign)
[![Tests](https://img.shields.io/badge/Tests-Gradle%20test-success)](#build-and-test)
[![Modules](https://img.shields.io/badge/Modules-http%20%7C%20utils-informational)](#project-modules)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)


A lightweight multi-module wrapper around OpenFeign with:
- centralized service configuration via Owner
- pluggable encoders/decoders
- request/response interceptors
- token provider + cache abstraction for authorization flows
- retry policy abstraction
- raw HTTP response support

## Project Modules

- `utils`
  - Generic helper utilities (`StringUtil`, `MapUtil`, `DurationUtil`)
  - JSON extraction helper (`JacksonExtractor`)
  - `ServiceConfigFactory` for Owner-based service config creation

- `http`
  - `HttpClient` builder for typed Feign clients
  - Config contract `ServiceConfig`
  - Encoders/decoders wrappers
  - Request/response interceptor adapters
  - Logging abstraction (`HttpLogger`, `Slf4jLogger`)
  - Authorization components (credentials, token provider, token cache, token handler)

## Tech Stack

- Java + Gradle (multi-module)
- OpenFeign `13.6`
- Owner `1.0.12`
- SLF4J `2.0.17`
- Log4j2 `2.25.3` (`log4j-slf4j2-impl` binding)
- JUnit 5 + WireMock for tests

## Requirements

- JDK 17+
- Gradle Wrapper (`./gradlew`)

## Build and Test

```bash
./gradlew clean build
```

Run all tests:

```bash
./gradlew test
```

Run module tests:

```bash
./gradlew :utils:test
./gradlew :http:test
```

## Configuration Model

Service configuration uses the `${service}` prefix and is loaded from:
- environment variables
- JVM system properties

Supported keys:
- `${service}.base.url`
- `${service}.token`
- `${service}.api.key`
- `${service}.connect.timeout`
- `${service}.read.timeout`

Example (`test-service`):

```bash
-Dtest.service.base.url=http://localhost:8080
-Dtest.service.token=Bearer my-token
-Dtest.service.api.key=my-api-key
-Dtest.service.connect.timeout=5000
-Dtest.service.read.timeout=10000
```

## Quick Start

### 1. Build a simple typed client

```java
interface TestService {
    @RequestLine("GET /test")
    RawHttpResponse sendTest();
}

TestService client = HttpClient.builder()
        .fromServiceConfig("test-service")
        .build(TestService.class);

RawHttpResponse response = client.sendTest();
```

### 2. Add custom interceptors and retry policy

```java
TestService client = HttpClient.builder()
        .fromServiceConfig("test-service")
        .requestInterceptor(template -> template.header("X-Correlation-Id", "123"))
        .retryPolicy(RetryPolicy.fixedRetry(5, Duration.ofMillis(100)))
        .build(TestService.class);
```

### 3. Use demo auth interceptor

```java
TestService client = HttpClient.builder()
        .fromServiceConfig("test-service")
        .requestInterceptor(new DemoAuthRequestInterceptor(
                "client_credentials",
                "client-id",
                "client-secret"
        ))
        .build(TestService.class);
```

## Logging

The project uses Log4j2 with SLF4J 2.x bridge via dependency bundle `logging-log4j`:
- `slf4j-api`
- `log4j-slf4j2-impl`
- `log4j-core`
- `log4j-api`

This avoids SLF4J 1.7 binding conflicts and enables proper SLF4J provider resolution.

## Architecture Notes

- `HttpClient` wraps Feign builder configuration and exposes a fluent API.
- Request/response interception is implemented via wrapper interfaces to keep custom code Feign-agnostic.
- Token management is split into distinct responsibilities:
  - `TokenProvider` for authorize/refresh logic
  - `TokenCache` for storage
  - `TokenHandler` for orchestration
- `RawHttpResponseDecoder` + `RawHttpResponseInterceptor` support low-level response handling for clients that return raw payloads.

## License

This project is distributed under the license defined in [`LICENSE`](LICENSE).
