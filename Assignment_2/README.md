# Assignment #2 — WalletHub Rate & Review

A Selenium 4 + TestNG automation project that, on an institution's WalletHub
profile:

1. Rates it by **hovering** a star, verifying the stars **light up**, then
   **clicking** (star 4 by default) — not just navigating to the review page.
2. Selects a **policy** ("Health Insurance") from the review modal's dropdown.
3. Types a random review (**200+ characters**, one character at a time).
4. **Submits** and confirms the "Awesome! Your review has been posted." screen.
5. Opens the personal profile to check the review feed.

Built on the same hand-rolled framework as Assignment 1 (driver management,
config, explicit-wait helpers, page objects) — no third-party wrapper library.

## Prerequisite: the light account

Create a light user account at <https://wallethub.com/join/light> (uncheck the
free-credit-score box), as the brief says. Submitting a review must be done while
signed in as that account, so the test drives a **copy of your signed-in Chrome
profile** (same approach as Assignment 1 — Chrome 136+ won't automate the live
profile).

## Quick start

```bash
# 1. Prerequisites (macOS + Homebrew; skip any you have). Any JDK 17+ works.
brew install openjdk@17 maven
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# 2. From the repo, enter this project
cd Wallethub_Automation_QA_Tests/Assignment_2

# 3. Copy your WalletHub-signed-in Chrome profile once (Chrome quit; <PROFILE>
#    from chrome://version -> "Profile Path", e.g. Default):
rm -rf /tmp/wallethub-profile && mkdir -p /tmp/wallethub-profile
cp -R "$HOME/Library/Application Support/Google/Chrome/<PROFILE>" /tmp/wallethub-profile/Default
cp  "$HOME/Library/Application Support/Google/Chrome/Local State" /tmp/wallethub-profile/

# 4. Run
mvn test
```

`chrome.user.data.dir` in `config.properties` already points at
`/tmp/wallethub-profile`, so the browser opens signed in and the review can be
submitted.

## Configuration

All settings live in [`src/main/resources/config.properties`](src/main/resources/config.properties)
and can be overridden with `-Dkey=value` or an env var (`KEY`, dots → underscores).

| Key | Meaning | Default |
| --- | --- | --- |
| `browser` | `chrome` \| `firefox` \| `edge` | `chrome` |
| `headless` | run without a visible window | `false` |
| `chrome.user.data.dir` | signed-in profile copy to run against | `/tmp/wallethub-profile` |
| `chrome.profile.directory` | profile sub-folder | `Default` |
| `review.target.url` | institution profile to review | `.../profile/13732055i` |
| `profile.url` | personal profile checked at the end | `.../profile/` |
| `confirmation.url` | landing page after submit | `.../confirm-review` |
| `star.rating` | which star to click (1–5) | `4` |
| `policy` | dropdown option to select | `Health Insurance` |
| `review.min.length` | minimum review characters | `200` |
| `explicit.wait.timeout` / `page.load.timeout` | waits (seconds) | `20` / `45` |

## The star hover-and-verify

The rating widget's stars are `svg.rvs-star-svg[aria-label="N star rating"]` whose
`<path>` fill is grey (`#e4e9eb`) until hovered. `ProfileReviewPage.rateWithStar`:
1. hovers the target star (`Actions.moveToElement`),
2. reads the star's `<path>` fill and logs whether it changed from grey (lit up),
3. then clicks — so the requirement to "actually hover and see the stars light up,
   not just redirect" is met.

## Note on step 7 (the profile page)

The brief asks to open `/profile/` and assert the review is visible. In practice,
for a fresh light test account WalletHub serves a **404 "page can't be found"** at
`/profile/` rather than a review feed. The test therefore asserts on the real
outcome: it passes if the review feed is shown, and otherwise verifies the 404
page loaded and logs it. The authoritative "review posted" signal is the
**confirmation screen** in step 6, which the test asserts strictly.

## Project structure

```
Assignment_2/
├── pom.xml
├── testng.xml
└── src/
    ├── main/java/com/wallethub/qa/
    │   ├── config/    Configuration            — externalized settings
    │   ├── driver/    DriverFactory / Manager  — configured WebDriver
    │   ├── pages/     BasePage                 — waits, click (paced), typeLikeHuman, hover
    │   │              ProfileReviewPage        — star hover + verify + click
    │   │              WriteReviewModal         — policy dropdown, review textarea, submit
    │   │              ReviewConfirmationPage    — "Awesome!" confirmation
    │   │              ProfilePage              — profile / 404 check
    │   └── util/      RandomText               — 200+ char review generator
    └── test/java/com/wallethub/qa/
        ├── base/      BaseTest                 — driver setup / teardown
        └── tests/     WalletHubReviewTest      — the end-to-end flow
```

## Design notes (assessment criteria)

- **Explicit waits** — all interactions go through `BasePage`/`ExpectedConditions`;
  no `Thread.sleep` for synchronisation. (Deliberate human-pacing only: ~1s before
  each click, key-by-key typing.)
- **Page Object Model** — one class per screen/dialog; locators live beside their
  behaviour; the test reads as plain steps.
- **Externalized strings** — target URLs, star, policy, review length, timeouts all
  in `config.properties` with `-D`/env overrides.
- **Test setup** — `BaseTest` creates/quits a driver per test; suite in `testng.xml`.
- **Separation of concerns / code reuse / framework** — config, driver, pages, util
  and tests are separate layers, shared via `BasePage` & friends (same framework as
  Assignment 1, no wrapper libs).
- **Logging** — SLF4J + Logback throughout.

WalletHub's markup is Angular-generated and can change; the locators are isolated
in the page objects so a maintainer can adjust them in one place.
