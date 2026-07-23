package com.wallethub.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The write-review modal that opens after picking a star rating. Lets you choose
 * a policy/category, type the review text, and submit.
 *
 * <p>The policy control is a custom (non-native) dropdown: a placeholder
 * {@code <span class="dropdown-placeholder">} that reveals a
 * {@code <ul class="dropdown-list">} of {@code <li role="option">} items - so it
 * is driven by clicks, not Selenium's {@code Select}.
 */
public class WriteReviewModal extends BasePage {

    private static final By MODAL = By.cssSelector("write-review");
    private static final By POLICY_DROPDOWN = By.cssSelector("write-review .dropdown-placeholder");
    private static final By REVIEW_TEXTAREA = By.cssSelector("write-review textarea.wrev-user-input");
    private static final By SUBMIT_BUTTON =
            By.xpath("//write-review//button[normalize-space()='Submit']");

    public WriteReviewModal(WebDriver driver) {
        super(driver);
        waitForVisible(MODAL);   // ensure the dialog has actually opened
    }

    /**
     * Opens the policy dropdown and selects the option with the given label.
     *
     * @param policy e.g. "Health Insurance"
     * @return this modal, for chaining
     */
    public WriteReviewModal selectPolicy(String policy) {
        log.info("Selecting policy '{}'", policy);
        click(POLICY_DROPDOWN);
        By option = By.xpath("//write-review//ul[contains(@class,'dropdown-list')]"
                + "//li[normalize-space()='" + policy + "']");
        click(option);
        return this;
    }

    /**
     * Types the review text into the textarea, one character at a time.
     *
     * @param text the review body
     * @return this modal, for chaining
     */
    public WriteReviewModal writeReview(String text) {
        log.info("Typing a {}-character review", text.length());
        WebElement area = waitForVisible(REVIEW_TEXTAREA);
        area.click();
        typeLikeHuman(area, text);
        return this;
    }

    /**
     * Presses Submit.
     *
     * @return the {@link ReviewConfirmationPage} shown after a successful submit
     */
    public ReviewConfirmationPage submit() {
        log.info("Submitting the review");
        click(SUBMIT_BUTTON);
        return new ReviewConfirmationPage(driver);
    }
}
