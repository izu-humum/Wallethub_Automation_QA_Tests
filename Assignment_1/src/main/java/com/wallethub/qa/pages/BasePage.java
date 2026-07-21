package com.wallethub.qa.pages;

import com.wallethub.qa.config.Configuration;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all page objects.
 *
 * <p>It owns the {@link WebDriver} and a single {@link WebDriverWait}, and
 * exposes small, reusable interaction helpers that <em>always</em> wait
 * explicitly for the right condition before acting. Concrete pages therefore
 * never touch raw waits or {@code Thread.sleep}, which keeps them readable and
 * keeps the waiting strategy in one place.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Configuration.explicitWaitTimeout());
    }

    /** Opens the given URL. */
    protected void navigateTo(String url) {
        log.info("Navigating to {}", url);
        driver.get(url);
    }

    /** Waits until the element is present and visible, then returns it. */
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Waits until the element is visible and enabled, then returns it. */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Waits for the element to be clickable and clicks it. */
    protected void click(By locator) {
        log.debug("Clicking {}", locator);
        waitForClickable(locator).click();
    }

    /** Waits for the element to be visible, clears it and types the given text. */
    protected void type(By locator, CharSequence text) {
        log.debug("Typing into {}", locator);
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * @return {@code true} if the element becomes visible within the wait
     *         timeout, {@code false} otherwise (never throws)
     */
    protected boolean isVisible(By locator) {
        try {
            return waitForVisible(locator).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Like {@link #isVisible(By)} but with a caller-supplied timeout. Use for
     * optional or branching elements (e.g. a cookie banner) so the suite does
     * not wait the full explicit-wait timeout when the element is absent.
     *
     * @return {@code true} if visible within {@code timeout}, else {@code false}
     */
    protected boolean isVisibleWithin(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Waits until the element identified by {@code locator} is gone. */
    protected void waitForInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
}
