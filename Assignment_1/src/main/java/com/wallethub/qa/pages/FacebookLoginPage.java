package com.wallethub.qa.pages;

import com.wallethub.qa.config.Configuration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The Facebook login screen ({@code https://www.facebook.com}).
 *
 * <p>Locators are kept here, next to the behaviour that uses them, following the
 * Page Object pattern. The email / password / login-button locators are stable;
 * the cookie-consent banner only appears in some regions and is handled
 * best-effort so the flow works with or without it.
 */
public class FacebookLoginPage extends BasePage {

    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("pass");
    private static final By LOGIN_BUTTON = By.name("login");

    // Cookie-consent banner: its markup / labels vary by region and change
    // often. Prefer the privacy-preserving "decline" control; fall back to accept.
    private static final By DECLINE_COOKIES = By.cssSelector(
            "[data-testid='cookie-policy-manage-dialog-decline-button'],"
                    + "[aria-label='Decline optional cookies']");
    private static final By ACCEPT_COOKIES = By.cssSelector(
            "[data-cookiebanner='accept_button'],"
                    + "[data-testid='cookie-policy-manage-dialog-accept-button'],"
                    + "[aria-label='Allow all cookies']");

    public FacebookLoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Opens Facebook and dismisses the cookie banner if one appears.
     *
     * @return this page, for fluent chaining
     */
    public FacebookLoginPage open(String baseUrl) {
        navigateTo(baseUrl);
        dismissCookieBanner();
        return this;
    }

    /**
     * Signs in when the login form is shown. If a session is already active
     * (e.g. when reusing a Chrome profile that is still logged in), the login
     * step is skipped. Either way the caller lands on the {@link FacebookHomePage}.
     *
     * @param username Facebook email or phone
     * @param password Facebook password
     * @return the home page shown once signed in
     */
    public FacebookHomePage login(String username, String password) {
        if (isLoginFormDisplayed()) {
            log.info("Logging in as {}", username);   // never log the password
            type(EMAIL, username);
            type(PASSWORD, password);
            click(LOGIN_BUTTON);
        } else {
            log.info("Login form not shown - continuing (either already signed in, "
                    + "or Facebook served a consent/checkpoint page)");
        }
        return new FacebookHomePage(driver);
    }

    /** @return {@code true} if the login form is present (i.e. we are logged out). */
    public boolean isLoginFormDisplayed() {
        return isVisibleWithin(EMAIL, Configuration.optionalWaitTimeout());
    }

    /** Best-effort, privacy-preserving dismissal of the cookie-consent banner. */
    private void dismissCookieBanner() {
        if (isVisibleWithin(DECLINE_COOKIES, Configuration.optionalWaitTimeout())) {
            log.info("Cookie banner detected - declining optional cookies");
            click(DECLINE_COOKIES);
        } else if (isVisibleWithin(ACCEPT_COOKIES, Configuration.optionalWaitTimeout())) {
            log.info("Cookie banner detected - accepting to continue");
            click(ACCEPT_COOKIES);
        }
    }
}
