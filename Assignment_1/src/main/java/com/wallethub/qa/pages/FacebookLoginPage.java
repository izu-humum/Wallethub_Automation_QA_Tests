package com.wallethub.qa.pages;

import java.time.Duration;
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
     * Types the credentials into the login form and submits. If a session is
     * already active (e.g. when attached to a Chrome you signed into by hand),
     * the login form is absent and this step is skipped.
     *
     * @param username Facebook email or phone
     * @param password Facebook password
     * @return the {@link FacebookHomePage}
     */
    public FacebookHomePage login(String username, String password) {
        if (isVisibleWithin(EMAIL, Duration.ofSeconds(5))) {
            log.info("Logging in as {}", username);   // never log the password
            type(EMAIL, username);
            type(PASSWORD, password);
            click(LOGIN_BUTTON);
        } else {
            log.info("Login form not shown - already signed in, going to the feed");
        }
        return new FacebookHomePage(driver);
    }
}
