package com.wallethub.qa.driver;

import org.openqa.selenium.WebDriver;

/**
 * Holds the {@link WebDriver} for the current thread.
 *
 * <p>Using a {@link ThreadLocal} keeps each test thread bound to its own driver,
 * so the suite stays correct if tests are later run in parallel &mdash; without
 * passing the driver around by hand.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
        // Utility class - not instantiable.
    }

    /**
     * @return the driver bound to the current thread
     * @throws IllegalStateException if no driver has been set
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "No WebDriver bound to this thread. Was setDriver() called in test setup?");
        }
        return driver;
    }

    public static void setDriver(WebDriver driver) {
        DRIVER.set(driver);
    }

    /**
     * Quits the current driver (if any) and unbinds it from the thread.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
