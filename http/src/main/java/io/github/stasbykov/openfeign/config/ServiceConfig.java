package io.github.stasbykov.openfeign.config;

import org.aeonbits.owner.Config;

import static io.github.stasbykov.openfeign.constants.Configuration.DEFAULT_CONNECT_TIMEOUT;
import static io.github.stasbykov.openfeign.constants.Configuration.DEFAULT_READ_TIMEOUT;

/**
 * Service configuration contract loaded through Owner.
 *
 * <p>Supported keys:</p>
 * <ul>
 *     <li>${service}.base.url: service base URL</li>
 *     <li>${service}.token: bearer token used for authentication</li>
 *     <li>${service}.api.key: API key value</li>
 *     <li>${service}.connect.timeout: connection timeout in milliseconds</li>
 *     <li>${service}.read.timeout: read timeout in milliseconds</li>
 * </ul>
 *
 * <p>Values can be provided by environment variables and JVM system properties.</p>
 *
 * @see org.aeonbits.owner.Config
 * @see org.aeonbits.owner.Config.Sources
 * @see org.aeonbits.owner.Config.LoadPolicy
 * @see org.aeonbits.owner.Config.LoadType
 * @see org.aeonbits.owner.Config.Key
 * @see org.aeonbits.owner.Config.DefaultValue
 */
@Config.Sources({
        "system:env",
        "system:properties"
})
@Config.LoadPolicy(Config.LoadType.MERGE)
public interface ServiceConfig extends Config {
    /**
     * @return service base URL
     */
    @Key("${service}.base.url")
    String baseUrl();

    /**
     * @return static bearer token
     */
    @Key("${service}.token")
    String token();


    /**
     * @return service API key
     */
    @Key("${service}.api.key")
    String apiKey();

    /**
     * @return connection timeout in milliseconds
     */
    @Key("${service}.connect.timeout")
    @DefaultValue(DEFAULT_CONNECT_TIMEOUT)
    int connectTimeout();

    /**
     * @return read timeout in milliseconds
     */
    @Key("${service}.read.timeout")
    @DefaultValue(DEFAULT_READ_TIMEOUT)
    int readTimeout();
}

