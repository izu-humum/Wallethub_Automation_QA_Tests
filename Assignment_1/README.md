# Assignment #1 — Facebook Login & Post a Status

A Selenium 4 + TestNG automation project that:

1. Logs in to Facebook with credentials supplied through a changeable variable.
2. Posts the status message `Hello World`.

The framework layer (driver management, configuration, waits, page objects) is
implemented from scratch rather than relying on a third-party wrapper library.

## Requirements

- **JDK 17+**
- **Maven 3.8+**
- A local **Chrome** (default), **Firefox**, or **Edge** browser. Driver
  binaries are resolved automatically by Selenium Manager — nothing to install.

## Configuration

All settings live in [`src/main/resources/config.properties`](src/main/resources/config.properties)
and can be overridden **without editing the file**. Resolution order (highest
priority first):

1. JVM system property — `-Dfb.username=me@example.com`
2. Environment variable — `FB_USERNAME` (key upper-cased, dots → underscores)
3. `config.properties`

| Key | Meaning | Default |
| --- | --- | --- |
| `browser` | `chrome` \| `firefox` \| `edge` | `chrome` |
| `headless` | run without a visible window | `false` |
| `chrome.use.existing.profile` | drive your real Chrome profile (see below) | `true` |
| `chrome.user.data.dir` | Chrome user-data dir (blank ⇒ auto-detect) | _blank_ |
| `chrome.profile.directory` | profile sub-directory | `Default` |
| `explicit.wait.timeout` | explicit-wait timeout (seconds) | `15` |
| `page.load.timeout` | page-load timeout (seconds) | `30` |
| `base.url` | application URL | `https://www.facebook.com` |
| `fb.username` | Facebook email / phone | _placeholder_ |
| `fb.password` | Facebook password | _placeholder_ |
| `fb.status.message` | status text to post | `Hello World` |

> **Never commit real credentials.** The checked-in values are placeholders;
> pass real ones at run time as shown below.

### Reusing your signed-in Chrome session (CAPTCHA avoidance)

By default the tests drive your **real Chrome profile**
(`chrome.use.existing.profile=true`) rather than the empty, temporary profile
Selenium would otherwise create. Reusing the profile keeps your cookies and
signed-in state, so the site sees a returning user instead of a brand-new
(incognito-like) session — which greatly reduces CAPTCHA / bot challenges.

- The user-data directory is auto-detected per OS when `chrome.user.data.dir` is
  blank (macOS `~/Library/Application Support/Google/Chrome`, Windows
  `%LOCALAPPDATA%\Google\Chrome\User Data`, Linux `~/.config/google-chrome`), or
  set it explicitly. `chrome.profile.directory` selects the profile (e.g. `Default`).
- **Quit Chrome before running** — Chrome will not share a profile that a running
  instance already holds (`user data directory is already in use`).
- To opt out and use a clean throwaway profile: `-Dchrome.use.existing.profile=false`.
- Applied to Chrome (the default browser).

## Running

From this folder (`Assignment_1/`):

```bash
# Provide credentials on the command line (recommended)
mvn test -Dfb.username="me@example.com" -Dfb.password="••••••"

# …or export them as environment variables
export FB_USERNAME="me@example.com"
export FB_PASSWORD="••••••"
mvn test

# Run headless / in another browser
mvn test -Dheadless=true -Dbrowser=firefox
```

The suite is defined in [`testng.xml`](testng.xml) and wired into Maven via the
Surefire plugin.

## Project structure

```
Assignment_1/
├── pom.xml                     Maven build + pinned dependency versions
├── testng.xml                  TestNG suite definition
└── src/
    ├── main/
    │   ├── java/com/wallethub/qa/
    │   │   ├── config/         Configuration     — externalized settings loader
    │   │   ├── driver/         DriverFactory     — builds a configured WebDriver
    │   │   │                   DriverManager     — thread-local driver holder
    │   │   └── pages/          BasePage          — reusable explicit-wait helpers
    │   │                       FacebookLoginPage — login screen
    │   │                       FacebookHomePage  — feed + post composer
    │   └── resources/
    │       ├── config.properties
    │       └── logback.xml     Logging configuration
    └── test/
        └── java/com/wallethub/qa/
            ├── base/           BaseTest          — driver setup / teardown
            └── tests/          FacebookPostStatusTest
```

## Design notes (how the assessment criteria are met)

- **Explicit waits** — every interaction goes through `BasePage`, which waits on
  the appropriate `ExpectedConditions`. There is no `Thread.sleep` and no
  implicit wait.
- **Page Object Model** — one class per screen; locators live beside the
  behaviour that uses them; tests speak only in page-object methods.
- **Externalized strings** — URLs, credentials, timeouts and test data come from
  `config.properties` with system-property / environment-variable overrides.
- **Test setup** — `BaseTest` creates a fresh driver per test and quits it
  afterwards; the suite is declared in `testng.xml`.
- **Separation of concerns** — configuration, driver lifecycle, pages and tests
  are in distinct packages/layers.
- **Code reuse** — `BasePage`, `DriverFactory`, `DriverManager` and
  `Configuration` are shared building blocks.
- **Logging** — SLF4J + Logback; the password is never logged.

## Notes on automating Facebook

Facebook actively resists automation. A real run may hit a login checkpoint,
two-factor prompt, or "suspicious login" review, and the composer markup is
obfuscated and localized. The composer locators in `FacebookHomePage` target the
English UI's ARIA roles/labels and are the most likely thing to need updating —
they are deliberately isolated so a maintainer can adjust them in one place.
