package com.wallethub.qa.tests;

import com.wallethub.qa.base.BaseTest;
import com.wallethub.qa.config.Configuration;
import com.wallethub.qa.pages.FacebookHomePage;
import com.wallethub.qa.pages.FacebookLoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Assignment 1: log into Facebook with configurable credentials and post a
 * status message.
 *
 * <p>The username, password and status text all come from {@link Configuration}
 * (see {@code config.properties}), so they can be changed without touching this
 * test - satisfying the "credentials on a variable we can change" requirement.
 */
public class FacebookPostStatusTest extends BaseTest {

    @Test(description = "Logs into Facebook and posts a status message")
    public void shouldLoginAndPostStatus() {
        String username = Configuration.facebookUsername();
        String password = Configuration.facebookPassword();
        String statusMessage = Configuration.statusMessage();

        // 1. Log in (skipped automatically if the reused profile is already signed in).
        FacebookHomePage home = new FacebookLoginPage(driver())
                .open(Configuration.baseUrl())
                .login(username, password);

        Assert.assertTrue(home.isLoaded(),
                "Login appears to have failed - the news feed did not load. "
                        + "Check the credentials or a Facebook security checkpoint.");

        // 2. Post the status message.
        home.postStatus(statusMessage);

        // 3. Verify the post reached the feed.
        Assert.assertTrue(home.isStatusVisible(statusMessage),
                "The status message was not found in the feed after posting.");
    }
}
