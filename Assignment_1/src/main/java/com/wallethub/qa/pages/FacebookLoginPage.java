package com.wallethub.qa.pages;

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
    private static final By ACCEPT_COOKIES =
            By.cssSelector("[data-cookiebanner='accept_button'], [data-testid='cookie-policy-manage-dialog-accept-button']");

    public FacebookLoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Opens the Facebook home/login page and dismisses the cookie banner if present.
     *
     * @return this page, for fluent chaining
     */
    public FacebookLoginPage open(String baseUrl) {
        navigateTo(baseUrl);
        acceptCookiesIfPresent();
        return this;
    }

    /**
     * Logs in with the supplied credentials.
     *
     * @param username Facebook email or phone
     * @param password Facebook password
     * @return the {@link FacebookHomePage} shown after a successful login
     */
    public FacebookHomePage loginAs(String username, String password) {
        // Never log the password - only the (non-secret) username.
        log.info("Logging in as {}", username);
        type(EMAIL, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
        return new FacebookHomePage(driver);
    }

    private void acceptCookiesIfPresent() {
        if (isVisible(ACCEPT_COOKIES)) {
            log.info("Cookie banner detected - accepting to continue");
            click(ACCEPT_COOKIES);
        }
    }
}
