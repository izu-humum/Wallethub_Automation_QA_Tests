package com.wallethub.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The confirmation page shown at {@code /confirm-review} after a review is
 * submitted ("Awesome! / Your review has been posted.").
 */
public class ReviewConfirmationPage extends BasePage {

    private static final By HEADING = By.cssSelector(".confirm-review .rvc-header h2");
    private static final By SUBHEADING = By.cssSelector(".confirm-review .rvc-header h4");
    private static final By POSTED_REVIEW = By.cssSelector(".confirm-review .rvc-body-middle");

    public ReviewConfirmationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * @return {@code true} once the confirmation screen is shown (waits for it,
     *         since the site takes a moment after submit)
     */
    public boolean isConfirmed() {
        return isVisible(HEADING) && isVisible(SUBHEADING);
    }

    /** @return the big heading, e.g. "Awesome!". */
    public String heading() {
        return waitForVisible(HEADING).getText().trim();
    }

    /** @return the sub-heading, e.g. "Your review has been posted.". */
    public String subHeading() {
        return waitForVisible(SUBHEADING).getText().trim();
    }

    /** @return the review text echoed back on the confirmation page (best-effort). */
    public String postedReviewText() {
        return isVisible(POSTED_REVIEW) ? waitForVisible(POSTED_REVIEW).getText().trim() : "";
    }
}
