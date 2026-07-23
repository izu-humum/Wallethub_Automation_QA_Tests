# Assignment #1 — Facebook Login & Post a Status

A Selenium 4 + TestNG automation project that:

1. Logs in to Facebook with credentials supplied through a changeable variable.
2. Posts the status message `Hello World`.

The framework layer (driver management, configuration, waits, page objects) is
implemented from scratch rather than relying on a third-party wrapper library.

## Quick start (first run)

```bash
# 1. Install prerequisites (macOS + Homebrew; skip any you already have)
brew install openjdk@17 maven
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"     # any JDK 17+ works

# 2. Get the code
git clone https://github.com/izu-humum/Wallethub_Automation_QA_Tests.git
cd Wallethub_Automation_QA_Tests/Assignment_1

# 3. Copy your signed-in Chrome profile so the test runs as the real, logged-in
#    you. Sign in to Facebook in Chrome, then QUIT Chrome. <PROFILE> is your
#    profile folder from chrome://version -> "Profile Path" (e.g. Default):
rm -rf /tmp/fb-profile && mkdir -p /tmp/fb-profile
cp -R "$HOME/Library/Application Support/Google/Chrome/<PROFILE>" /tmp/fb-profile/Default
cp  "$HOME/Library/Application Support/Google/Chrome/Local State" /tmp/fb-profile/

# 4. Run
mvn test -Dchrome.user.data.dir=/tmp/fb-profile
```

Chrome opens **already signed in** (your copied session), skips the login form,
and posts "Hello World" — no CAPTCHA, because it's your real logged-in profile.
Chrome and a matching driver are handled automatically by Selenium Manager.

The one manual step is that `cp`: copying a browser profile can't be scripted
(it's blocked as a credential-copy pattern), so you run it once yourself.

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
| `chrome.user.data.dir` | Chrome profile dir to run against (a copy of yours) | `/tmp/fb-profile` |
| `chrome.profile.directory` | profile sub-folder inside it | `Default` |
| `explicit.wait.timeout` | explicit-wait timeout (seconds) | `15` |
| `page.load.timeout` | page-load timeout (seconds) | `30` |
| `base.url` | application URL | `https://www.facebook.com` |
| `fb.username` | Facebook email / phone | _placeholder_ |
| `fb.password` | Facebook password | _placeholder_ |
| `fb.status.message` | status text to post | `Hello World` |

### Providing credentials

**Plug and play:** set `fb.username` and `fb.password` in
[`config.properties`](src/main/resources/config.properties), then run `mvn test`.

Prefer not to edit the file? Override them at run time instead:

```bash
mvn test -Dfb.username="me@example.com" -Dfb.password="secret"
```

The password is never logged.

## Running

From this folder (`Assignment_1/`), after the one-time profile copy (see
Quick start above):

```bash
mvn test -Dchrome.user.data.dir=/tmp/fb-profile

# headless, or a different browser:
mvn test -Dchrome.user.data.dir=/tmp/fb-profile -Dheadless=true
mvn test -Dchrome.user.data.dir=/tmp/fb-profile -Dbrowser=edge
```

`chrome.user.data.dir` already has this value in `config.properties`, so a plain
`mvn test` works too. The suite is defined in [`testng.xml`](testng.xml) and
wired into Maven via the Surefire plugin.

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
