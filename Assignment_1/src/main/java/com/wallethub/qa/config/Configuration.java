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
 *   <li>{@code config.local.properties} &mdash; an optional, git-ignored file
 *       for real credentials and machine-specific overrides</li>
 *   <li>{@code config.properties} on the class path (checked-in defaults)</li>
 * </ol>
 *
 * <p>This lets credentials and other data be changed at run time without
 * touching source or resource files, which keeps secrets out of version
 * control and makes the suite CI-friendly.
 */
public final class Configuration {

    private static final String BASE_RESOURCE = "config.properties";
    private static final String LOCAL_RESOURCE = "config.local.properties";
    private static final Properties FILE_PROPERTIES = loadLayeredProperties();

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

    // ---- Chrome profile (reuse the existing signed-in session) --------------

    /**
     * @return {@code true} if Chrome should launch against the real user profile
     *         instead of a throwaway one, so saved cookies / the signed-in
     *         session carry over and the browser is not treated as a fresh
     *         (incognito-like) session that tends to trigger CAPTCHAs
     */
    public static boolean useExistingChromeProfile() {
        return Boolean.parseBoolean(require("chrome.use.existing.profile"));
    }

    /**
     * @return an explicit Chrome user-data directory, or {@code null}/blank to
     *         let {@code DriverFactory} auto-detect the OS default location
     */
    public static String chromeUserDataDir() {
        return get("chrome.user.data.dir");
    }

    /** @return the profile sub-directory to use, e.g. {@code Default}. */
    public static String chromeProfileDirectory() {
        return require("chrome.profile.directory");
    }

    // ---- Timeouts -----------------------------------------------------------

    public static Duration explicitWaitTimeout() {
        return Duration.ofSeconds(requireLong("explicit.wait.timeout"));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(requireLong("page.load.timeout"));
    }

    /**
     * @return a short timeout for probing optional / branching elements (cookie
     *         banner, "already logged in?"), so the suite does not stall for the
     *         full explicit-wait timeout when such an element is absent
     */
    public static Duration optionalWaitTimeout() {
        return Duration.ofSeconds(requireLong("optional.wait.timeout"));
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
                            + BASE_RESOURCE + ", as -D" + key + "=... or via the "
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

    /**
     * Loads {@link #BASE_RESOURCE} and layers the optional, git-ignored
     * {@link #LOCAL_RESOURCE} on top (local values win). The local file is the
     * intended home for real credentials, so they never enter version control.
     */
    private static Properties loadLayeredProperties() {
        Properties merged = new Properties();
        merged.putAll(readResource(BASE_RESOURCE, true));
        Properties local = readResource(LOCAL_RESOURCE, false);
        if (local != null) {
            merged.putAll(local);
        }
        return merged;
    }

    /**
     * Reads a properties resource from the class path.
     *
     * @param resourceName the resource to read
     * @param required     whether the resource being absent is an error
     * @return the loaded properties, or {@code null} if optional and absent
     */
    private static Properties readResource(String resourceName, boolean required) {
        try (InputStream in = Configuration.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException(
                            resourceName + " was not found on the class path.");
                }
                return null;
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + resourceName, e);
        }
    }
}
