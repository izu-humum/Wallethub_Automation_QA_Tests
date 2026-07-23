package com.wallethub.qa.pages;

import com.wallethub.qa.config.Configuration;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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

    /** Deliberate human pace: ~1s before every click; ~60-150ms between keystrokes. */
    private static final long CLICK_DELAY_MS = 1000;
    private static final long KEYSTROKE_MIN_MS = 60;
    private static final long KEYSTROKE_JITTER_MS = 90;

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

    /** Moves the mouse over {@code locator} (e.g. to trigger a hover effect). */
    protected void hover(By locator) {
        hover(waitForVisible(locator));
    }

    /** Moves the mouse over the given element. */
    protected void hover(WebElement element) {
        new Actions(driver).moveToElement(element).perform();
    }

    /** Waits for the element to be clickable, pauses ~1s (human pace), then clicks. */
    protected void click(By locator) {
        log.debug("Clicking {}", locator);
        WebElement element = waitForClickable(locator);
        pause(CLICK_DELAY_MS);
        element.click();
    }

    /**
     * Waits for the element to be visible, clears it and types the text
     * character-by-character (see {@link #typeLikeHuman}).
     */
    protected void type(By locator, CharSequence text) {
        log.debug("Typing into {}", locator);
        WebElement element = waitForVisible(locator);
        element.clear();
        typeLikeHuman(element, text);
    }

    /**
     * Types into an already-located element one character at a time with a short,
     * slightly varied delay - mimicking a human and avoiding the instant-fill
     * pattern bot-checks look for. Works for plain inputs and contenteditable
     * editors alike (it does not clear first).
     */
    protected void typeLikeHuman(WebElement element, CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            element.sendKeys(Character.toString(text.charAt(i)));
            pause(KEYSTROKE_MIN_MS + (long) (Math.random() * KEYSTROKE_JITTER_MS));
        }
    }

    /** Sleeps for {@code millis}, preserving the thread's interrupt flag. */
    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
     * Like {@link #isVisible(By)} but with a caller-supplied timeout, for
     * optional / branching checks (e.g. "is the login form shown?") so the suite
     * does not stall for the full explicit-wait timeout when the element is absent.
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
