package com.wallethub.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The user's own WalletHub profile page ({@code /profile/}), visited at the end
 * of the flow to confirm the review.
 *
 * <p>Note: for the light test account created for this assignment, WalletHub
 * serves a 404 "page can't be found" here rather than a review feed, so this page
 * exposes both the (would-be) review-feed check and the actual error state.
 */
public class ProfilePage extends BasePage {

    private static final By ERROR_BOX = By.cssSelector(".error-page");
    private static final By ERROR_HEADING = By.cssSelector(".error-page .msg-container h1");
    /** A posted review in the feed would render as an article/review card. */
    private static final By REVIEW_FEED_ITEM = By.cssSelector("[class*='review']");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    /** Opens the profile page. */
    public ProfilePage open(String url) {
        navigateTo(url);
        return this;
    }

    /** @return {@code true} if WalletHub served its "not found" error page. */
    public boolean showsNotFound() {
        return isVisible(ERROR_BOX);
    }

    /** @return the error heading text, e.g. "Whoops, the page can't be found!". */
    public String errorHeading() {
        return isVisible(ERROR_HEADING) ? waitForVisible(ERROR_HEADING).getText().trim() : "";
    }

    /** @return {@code true} if a review feed item is visible (not the case for a 404). */
    public boolean showsReviewFeed() {
        return !showsNotFound() && !driver.findElements(REVIEW_FEED_ITEM).isEmpty();
    }
}
