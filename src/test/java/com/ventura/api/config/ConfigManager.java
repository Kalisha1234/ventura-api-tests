package com.ventura.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central place for environment configuration.
 *
 * Resolution order for the active environment: -Denv=&lt;name&gt; system property, then
 * ENV environment variable, defaulting to "dev". Each environment has a
 * classpath resource {@code config-<env>.properties}.
 *
 * Credentials and secrets are read only from environment variables / system properties
 * (never checked into a properties file) so the suite stays CI-safe.
 */
public final class ConfigManager {

    private static final String DEFAULT_ENV = "dev";
    private static final Properties PROPERTIES = load(resolveEnv());

    private ConfigManager() {
    }

    private static String resolveEnv() {
        return firstNonBlank(System.getProperty("env"), System.getenv("ENV"), DEFAULT_ENV);
    }

    private static Properties load(String env) {
        String resource = "config-" + env + ".properties";
        Properties props = new Properties();
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing config resource on classpath: " + resource);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resource, e);
        }
        return props;
    }

    public static String environment() {
        return resolveEnv();
    }

    public static String baseUri() {
        String uri = firstNonBlank(System.getProperty("base.uri"), System.getenv("BASE_URI"),
                PROPERTIES.getProperty("base.uri"));
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException(
                    "base.uri is not configured for environment '" + environment()
                            + "'. Set it in config-" + environment()
                            + ".properties or pass -Dbase.uri=...");
        }
        return uri;
    }

    /** Pre-issued bearer access token (highest-priority auth bootstrap strategy). */
    public static String bearerToken() {
        return firstNonBlank(System.getProperty("test.bearer.token"), System.getenv("TEST_BEARER_TOKEN"));
    }

    /** Refresh token paired with {@link #bearerToken()}, used once the access token expires. */
    public static String refreshToken() {
        return firstNonBlank(System.getProperty("test.refresh.token"), System.getenv("TEST_REFRESH_TOKEN"));
    }

    /** Email of an existing dev-environment user that already has a password set. */
    public static String testUserEmail() {
        return firstNonBlank(System.getProperty("test.user.email"), System.getenv("TEST_USER_EMAIL"));
    }

    public static String testUserPassword() {
        return firstNonBlank(System.getProperty("test.user.password"), System.getenv("TEST_USER_PASSWORD"));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
