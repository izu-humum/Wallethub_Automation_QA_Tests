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
| `chrome.debugger.address` | attach to a Chrome you launched (see below) | `127.0.0.1:9222` |
| `chrome.user.data.dir` | fresh-launch profile dir (fallback; see below) | _blank_ |
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

### Using your real logged-in session (avoids the bot / CAPTCHA check)

A fresh automated browser gets flagged as a bot at login. By default this
project **attaches to a Chrome you launch and sign into yourself**, so the test
reuses your real session — a human login is never flagged.

```bash
# 1. Start Chrome on a debug port and sign in to Facebook (keep it open):
./scripts/launch-chrome-debug.sh            # port 9222

# 2. Then just run — no flags:
mvn test
```

`chrome.debugger.address` defaults to `127.0.0.1:9222`, so `mvn test` attaches to
that Chrome, opens a tab there and — already signed in — skips login and posts.
If nothing is listening on that port it **falls back to a fresh browser**, so a
plain `mvn test` never breaks.

> The launcher uses a dedicated profile because Chrome 136+ won't expose the
> debug port on your normal profile (and won't let automation drive your live
> profile at all). Sign in there once; it persists.

**Alternative — copy a profile instead of attaching.** Blank
`chrome.debugger.address`, then point `chrome.user.data.dir` at a one-time copy
of your signed-in profile (`<PROFILE>` from `chrome://version` → "Profile Path"):

```bash
rm -rf /tmp/fb-profile && mkdir -p /tmp/fb-profile
cp -R "$HOME/Library/Application Support/Google/Chrome/<PROFILE>" /tmp/fb-profile/Default
cp  "$HOME/Library/Application Support/Google/Chrome/Local State" /tmp/fb-profile/
mvn test -Dchrome.debugger.address= -Dchrome.user.data.dir=/tmp/fb-profile
```

(The `cp` is manual — copying a browser's cookie store can't be scripted.)

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
