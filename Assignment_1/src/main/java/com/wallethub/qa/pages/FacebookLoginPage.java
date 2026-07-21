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

    // Facebook's element ids are randomly generated per render (e.g.
    // "_R_1h6kqsqppb6amH1_"), so we target the stable "name" attributes. The
    // login control is a <div> with no name/id, so it is located by its label.
    private static final By EMAIL = By.name("email");
    private static final By PASSWORD = By.name("pass");
    private static final By LOGIN_BUTTON = By.xpath(
            "//button[@name='login']"
                    + " | (//div[@role='none'][normalize-space()='Log in'])[1]"
                    + " | //span[normalize-space()='Log in']");

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
