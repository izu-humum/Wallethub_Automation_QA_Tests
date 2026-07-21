package com.wallethub.qa.base;

import com.wallethub.qa.driver.DriverFactory;
import com.wallethub.qa.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Common setup and teardown for every test.
 *
 * <p>A fresh {@link WebDriver} is created before each test method and quit
 * afterwards, so tests never share browser state. Subclasses reach the driver
 * through {@link #driver()}.
 */
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        log.info("=== Test setup: starting browser ===");
        DriverManager.setDriver(DriverFactory.createDriver());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        log.info("=== Test teardown: '{}' finished with status {} ===",
                result.getName(), statusText(result.getStatus()));
        DriverManager.quitDriver();
    }

    /** @return the WebDriver bound to the current test thread */
    protected WebDriver driver() {
        return DriverManager.getDriver();
    }

    private static String statusText(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "SUCCESS";
            case ITestResult.FAILURE -> "FAILURE";
            case ITestResult.SKIP -> "SKIP";
            default -> "UNKNOWN";
        };
    }
}
