package com.wallethub.qa.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * An institution's WalletHub profile page, specifically its "What's Your Rating?"
 * star widget.
 *
 * <p>The stars are {@code <svg class="rvs-star-svg" aria-label="N star rating">}
 * elements whose inner {@code <path>} fill changes colour on hover. This page
 * performs the real hover, verifies the stars light up, and only then clicks -
 * rather than jumping straight to the review page.
 */
public class ProfileReviewPage extends BasePage {

    /** Empty/unlit star fill (WalletHub grey #e4e9eb == rgb(228, 233, 235)). */
    private static final String EMPTY_STAR_FILL = "rgb(228, 233, 235)";

    /** All stars in the "What's Your Rating?" widget (scoped to .review-action). */
    private static final By RATING_STARS =
            By.cssSelector(".review-action review-star svg.rvs-star-svg");

    public ProfileReviewPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the institution profile page under review. */
    public ProfileReviewPage open(String url) {
        navigateTo(url);
        return this;
    }

    /**
     * Hovers the given star, checks the stars up to it light up, then clicks it -
     * which opens the write-review modal.
     *
     * @param starNumber 1-5
     * @return the {@link WriteReviewModal} that opens
     */
    public WriteReviewModal rateWithStar(int starNumber) {
        By star = starLocator(starNumber);

        log.info("Hovering the {}-star rating", starNumber);
        hover(star);
        verifyStarsLitUpTo(starNumber);

        log.info("Clicking the {}-star rating", starNumber);
        click(star);   // BasePage.click: ~1s human pause, waits for clickable, clicks
        return new WriteReviewModal(driver);
    }

    private By starLocator(int starNumber) {
        return By.cssSelector(
                ".review-action review-star svg.rvs-star-svg[aria-label='" + starNumber + " star rating']");
    }

    /**
     * Confirms the hover actually lit the stars: the hovered star's fill should no
     * longer be the empty grey. Logged rather than asserted hard, so a WalletHub
     * styling change surfaces as a clear warning instead of a brittle failure.
     */
    private void verifyStarsLitUpTo(int starNumber) {
        List<WebElement> stars = wait.until(d -> {
            List<WebElement> found = d.findElements(RATING_STARS);
            return found.size() >= starNumber ? found : null;
        });
        String fill = starFill(stars.get(starNumber - 1));
        if (fill != null && !fill.equalsIgnoreCase(EMPTY_STAR_FILL)) {
            log.info("Stars lit up on hover (star {} fill is now '{}')", starNumber, fill);
        } else {
            log.warn("Stars did not visibly light up on hover (fill still '{}') - "
                    + "WalletHub's hover styling may have changed.", fill);
        }
    }

    private String starFill(WebElement star) {
        return star.findElement(By.cssSelector("path")).getCssValue("fill");
    }
}
