package com.wallethub.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Central, read-only access point for every externalized setting.
 *
 * <p>Values come from {@code config.properties} and can be overridden at run
 * time (handy for CI, or to point at a different profile / target). Precedence,
 * highest first:
 * <ol>
 *   <li>JVM system property &mdash; e.g. {@code -Dstar.rating=3}</li>
 *   <li>Environment variable &mdash; the key upper-cased with dots turned into
 *       underscores, e.g. {@code STAR_RATING}</li>
 *   <li>{@code config.properties} on the class path</li>
 * </ol>
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

    /**
     * @return a Chrome user-data directory to launch against (e.g. a copy of your
     *         profile that is signed in to WalletHub), or {@code null}/blank to
     *         launch a fresh profile
     */
    public static String chromeUserDataDir() {
        return get("chrome.user.data.dir");
    }

    /** @return the profile sub-directory inside the user-data dir (default {@code Default}). */
    public static String chromeProfileDirectory() {
        String value = get("chrome.profile.directory");
        return (value != null && !value.isBlank()) ? value : "Default";
    }

    // ---- Timeouts -----------------------------------------------------------

    public static Duration explicitWaitTimeout() {
        return Duration.ofSeconds(requireLong("explicit.wait.timeout"));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(requireLong("page.load.timeout"));
    }

    // ---- Application under test / test data --------------------------------

    /** @return the profile page under review, e.g. {@code .../profile/13732055i}. */
    public static String reviewTargetUrl() {
        return require("review.target.url");
    }

    /** @return the personal profile page checked at the end, e.g. {@code .../profile/}. */
    public static String profileUrl() {
        return require("profile.url");
    }

    /** @return the URL the site lands on after a successful submit. */
    public static String confirmationUrl() {
        return require("confirmation.url");
    }

    /** @return which star to rate with (1-5). */
    public static int starRating() {
        return (int) requireLong("star.rating");
    }

    /** @return the policy/category to pick in the review dropdown, e.g. "Health Insurance". */
    public static String policy() {
        return require("policy");
    }

    /** @return the minimum length of the random review text. */
    public static int reviewMinLength() {
        return (int) requireLong("review.min.length");
    }

    // ---- Resolution machinery ----------------------------------------------

    /** Resolves a key or returns {@code null} if it is defined nowhere. */
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
