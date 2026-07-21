package com.wallethub.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The Facebook news-feed page shown after a successful login, from which a
 * status update can be published.
 *
 * <p><b>Locator note:</b> Facebook ships obfuscated, frequently-changing markup
 * and localizes its {@code aria-label}s. The composer locators below target the
 * stable ARIA roles/labels of the English UI and are the parts most likely to
 * need maintenance over time - which is exactly why they live here, isolated
 * from the test logic.
 */
public class FacebookHomePage extends BasePage {

    /** The "What's on your mind, <name>?" entry that opens the composer. */
    private static final By CREATE_POST_ENTRY =
            By.xpath("//span[contains(text(),'on your mind')]");

    /** The rich-text (Lexical) editor inside the open composer modal. */
    private static final By STATUS_TEXTBOX = By.cssSelector(
            "div[role='dialog'] div[role='textbox'],"
                    + "div[role='dialog'] div[contenteditable='true']");

    /** The "Post" button inside the composer modal (a &lt;div&gt; labelled "Post"). */
    private static final By POST_BUTTON = By.xpath(
            "//div[@role='dialog']//div[@role='none'][normalize-space()='Post']"
                    + " | //div[@role='dialog']//span[normalize-space()='Post']");

    public FacebookHomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * @return {@code true} once the feed is ready (the post entry point is
     *         visible), which confirms the login succeeded
     */
    public boolean isLoaded() {
        return isVisible(CREATE_POST_ENTRY);
    }

    /**
     * Opens the composer, types the message and publishes it.
     *
     * @param message the status text to post
     * @return this page, for fluent chaining
     */
    public FacebookHomePage postStatus(String message) {
        log.info("Posting status: \"{}\"", message);
        click(CREATE_POST_ENTRY);

        // The composer is a contenteditable (Lexical) editor: click to focus it,
        // then type straight in (no clear() - it starts empty).
        WebElement editor = waitForVisible(STATUS_TEXTBOX);
        editor.click();
        editor.sendKeys(message);

        click(POST_BUTTON);

        // The dialog closes when the post is submitted - use that as the signal.
        waitForInvisible(POST_BUTTON);
        return this;
    }

    /**
     * Best-effort check that a post with the given text is present in the feed.
     * May be affected by Facebook's asynchronous feed rendering.
     *
     * @param message the status text to look for
     * @return {@code true} if a feed post containing the text is visible
     */
    public boolean isStatusVisible(String message) {
        By post = By.xpath("//div[@role='article'][contains(., " + toXpathLiteral(message) + ")]");
        return isVisible(post);
    }

    /**
     * Builds a valid XPath string literal for arbitrary text, safely handling
     * embedded single and/or double quotes via {@code concat()}.
     */
    static String toXpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        StringBuilder concat = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                concat.append(", \"'\", ");
            }
            concat.append("'").append(parts[i]).append("'");
        }
        return concat.append(")").toString();
    }
}
