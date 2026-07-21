package com.wallethub.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The Facebook login screen ({@code https://www.facebook.com}).
 *
 * <p>Locators live here, next to the behaviour that uses them, following the
 * Page Object pattern.
 */
public class FacebookLoginPage extends BasePage {

    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("pass");
    private static final By LOGIN_BUTTON = By.name("login");

    public FacebookLoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Opens the Facebook login page.
     *
     * @return this page, for fluent chaining
     */
    public FacebookLoginPage open(String baseUrl) {
        navigateTo(baseUrl);
        return this;
    }

    /**
     * Types the credentials into the login form and submits.
     *
     * @param username Facebook email or phone
     * @param password Facebook password
     * @return the {@link FacebookHomePage} shown after logging in
     */
    public FacebookHomePage loginAs(String username, String password) {
        log.info("Logging in as {}", username);   // never log the password
        type(EMAIL, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
        return new FacebookHomePage(driver);
    }
}
