package com.wallethub.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Central, read-only access point for every externalized setting.
 *
 * <p>Values are resolved with the following precedence (highest first):
 * <ol>
 *   <li>JVM system property &mdash; e.g. {@code -Dfb.username=me@example.com}</li>
 *   <li>Environment variable &mdash; the key upper-cased with dots turned into
 *       underscores, e.g. {@code FB_USERNAME}</li>
 *   <li>{@code config.properties} on the class path</li>
 * </ol>
 *
 * <p>This lets credentials and other data be changed at run time without
 * touching source or resource files, which keeps secrets out of version
 * control and makes the suite CI-friendly.
 */
public final class Configuration {

    private static final String RESOURCE_NAME = "config.properties";
    private static final Properties FILE_PROPERTIES = loadFromClasspath();

    private Configuration() {
        // Utility class - not instantiable.
    }

    // ---- Browser / driver ---------------------------------------------------

    public static String browser() {
        return require("browser");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(require("headless"));
    }

    // ---- Timeouts -----------------------------------------------------------

    public static Duration explicitWaitTimeout() {
        return Duration.ofSeconds(requireLong("explicit.wait.timeout"));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(requireLong("page.load.timeout"));
    }

    // ---- Application under test --------------------------------------------

    public static String baseUrl() {
        return require("base.url");
    }

    // ---- Credentials / test data -------------------------------------------

    public static String facebookUsername() {
        return require("fb.username");
    }

    public static String facebookPassword() {
        return require("fb.password");
    }

    public static String statusMessage() {
        return require("fb.status.message");
    }

    // ---- Resolution machinery ----------------------------------------------

    /**
     * Resolves a key or returns {@code null} if it is defined nowhere.
     */
    public static String get(String key) {
        String fromSystem = System.getProperty(key);
        if (isPresent(fromSystem)) {
            return fromSystem.trim();
        }
        String fromEnv = System.getenv(toEnvVariable(key));
        if (isPresent(fromEnv)) {
            return fromEnv.trim();
        }
        String fromFile = FILE_PROPERTIES.getProperty(key);
        return isPresent(fromFile) ? fromFile.trim() : null;
    }

    /**
     * Resolves a key, failing fast with a clear message when it is missing.
     */
    private static String require(String key) {
        String value = get(key);
        if (value == null) {
            throw new IllegalStateException(
                    "Missing configuration for key '" + key + "'. Set it in "
                            + RESOURCE_NAME + ", as -D" + key + "=... or via the "
                            + toEnvVariable(key) + " environment variable.");
        }
        return value;
    }

    private static long requireLong(String key) {
        String value = require(key);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Configuration '" + key + "' must be a number but was '" + value + "'.", e);
        }
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static String toEnvVariable(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static Properties loadFromClasspath() {
        Properties properties = new Properties();
        try (InputStream in = Configuration.class.getClassLoader()
                .getResourceAsStream(RESOURCE_NAME)) {
            if (in == null) {
                throw new IllegalStateException(RESOURCE_NAME + " was not found on the class path.");
            }
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + RESOURCE_NAME, e);
        }
    }
}
