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

### Providing credentials

**Plug and play:** set `fb.username` and `fb.password` in
[`config.properties`](src/main/resources/config.properties), then run `mvn test`.

Prefer not to edit the file (e.g. to avoid committing secrets)? Override them at
run time instead:

```bash
mvn test -Dfb.username="me@example.com" -Dfb.password="secret"
```

The password is never logged. This repo is public, so use a throwaway test
account (or the `-D` override) rather than committing real credentials.

### Reusing a signed-in Chrome session (CAPTCHA avoidance)

By default the tests drive a **persistent Chrome profile**
(`chrome.use.existing.profile=true`) rather than the empty, temporary profile
Selenium would otherwise create. A profile that carries cookies and a signed-in
state looks like a returning user rather than a brand-new (incognito-like)
session, which greatly reduces CAPTCHA / bot challenges.

- **Chrome 136+ will not automate your *default* profile** — the browser exits on
  launch (`session not created: Chrome instance exited`). So a **dedicated**
  directory is used instead. `chrome.user.data.dir` blank ⇒
  `~/.wallethub-selenium/chrome-profile` (created on first run); the code rejects
  the default-profile path with a clear message if you point it there.
- **To reuse your real logged-in session** (best CAPTCHA avoidance), copy your
  Chrome profile into the dedicated directory once. Quit Chrome first, and be
  signed into Facebook in Chrome beforehand:
  ```bash
  rm -rf ~/.wallethub-selenium/chrome-profile
  mkdir -p ~/.wallethub-selenium/chrome-profile
  cp -R "$HOME/Library/Application Support/Google/Chrome/Default" ~/.wallethub-selenium/chrome-profile/Default
  cp  "$HOME/Library/Application Support/Google/Chrome/Local State" ~/.wallethub-selenium/chrome-profile/
  ```
  (Copy the `Default` profile itself — not the whole Chrome folder, which would
  nest as `chrome-profile/Chrome/...` and would not be found.)
- The login step **auto-detects state**: if the profile is already signed in, it
  skips the login form and goes straight to posting; otherwise it logs in.
- On launch the browser opens a single clean tab (saved-session restore and the
  crash-restore bubble are disabled).
- To opt out and use a clean throwaway profile: `-Dchrome.use.existing.profile=false`.
- Applied to Chrome (the default browser).

### Alternative: attach to a Chrome you launched (`debuggerAddress`)

Instead of copying a profile, let the test **attach** to a Chrome you start
yourself with a remote-debugging port. Selenium connects to that running browser
rather than launching its own, so its real session and tabs are reused — and
because *you* sign in by hand, the login is never treated as a bot.

```bash
# 1. Start Chrome on a debug port (dedicated profile; keep this window open):
./scripts/launch-chrome-debug.sh            # defaults to port 9222

# 2. Sign in to Facebook in that window (once).

# 3. In another terminal, run the test attached to it:
mvn test -Dchrome.debugger.address=127.0.0.1:9222
```

- A dedicated profile is used because Chrome 136+ won't expose the debug port on
  your default profile.
- `chrome.debugger.address` is blank by default, so a plain `mvn test` still
  launches its own browser — attach mode is purely opt-in.

## Running

From this folder (`Assignment_1/`):

```bash
# Credentials come from config.properties (edit fb.username / fb.password)
mvn test

# ...or override them at run time instead of editing the file:
mvn test -Dfb.username="me@example.com" -Dfb.password="secret"

# Run headless, or in another browser:
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
