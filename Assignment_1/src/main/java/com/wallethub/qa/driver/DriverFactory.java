package com.wallethub.qa.driver;

import com.wallethub.qa.config.Configuration;
import java.io.File;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a ready-to-use {@link WebDriver} for the browser named in
 * {@link Configuration#browser()}.
 *
 * <p>Driver binaries are resolved automatically by Selenium Manager (built into
 * Selenium 4), so no third-party driver-manager library is required.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
        // Utility class - not instantiable.
    }

    /**
     * Creates and configures a {@link WebDriver} instance.
     *
     * @return a new driver; the caller owns its lifecycle and must quit it
     * @throws IllegalArgumentException if the configured browser is unsupported
     */
    public static WebDriver createDriver() {
        String browser = Configuration.browser().toLowerCase();
        boolean headless = Configuration.headless();
        LOG.info("Creating '{}' driver (headless={})", browser, headless);

        WebDriver driver = switch (browser) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '" + browser + "'. Use chrome, firefox or edge.");
        };

        driver.manage().timeouts().pageLoadTimeout(Configuration.pageLoadTimeout());
        driver.manage().window().maximize();
        return driver;
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        String debuggerAddress = Configuration.chromeDebuggerAddress();
        if (debuggerAddress != null && !debuggerAddress.isBlank()) {
            // Attach to a Chrome you started with --remote-debugging-port. Selenium
            // connects to that running browser instead of launching its own, so the
            // test reuses that Chrome's real profile, signed-in session and tabs.
            // Because the browser is already running, launch-time arguments
            // (profile directory, headless, window size) do not apply here.
            options.setExperimentalOption("debuggerAddress", debuggerAddress.trim());
            LOG.info("Attaching to running Chrome at {} (no new browser is launched)",
                    debuggerAddress.trim());
            return options;
        }

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        applyExistingProfile(options);
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return options;
    }

    /**
     * Points Chrome at a persistent profile (when enabled) so the browser keeps
     * its cookies and signed-in session across runs. A returning, recognised
     * session is far less likely to face a CAPTCHA than a fresh, empty one.
     *
     * <p>A <em>dedicated</em> directory is used rather than Chrome's own default
     * profile, because Chrome 136+ refuses to let automation drive the default
     * profile (it hands off to any running Chrome and exits). Seed the dedicated
     * directory with a copy of your real profile to carry over a logged-in
     * session &mdash; see {@code config.properties}.
     */
    private static void applyExistingProfile(ChromeOptions options) {
        if (!Configuration.useExistingChromeProfile()) {
            return;
        }
        String userDataDir = resolveChromeUserDataDir();
        if (isDefaultChromeDir(userDataDir)) {
            throw new IllegalStateException(
                    "chrome.user.data.dir points at Chrome's default profile ('" + userDataDir
                            + "'), which Chrome 136+ will not automate (the browser exits on "
                            + "launch). Leave it blank to use the dedicated profile directory, or "
                            + "copy your profile to a separate folder and point here.");
        }
        options.addArguments("--user-data-dir=" + userDataDir);
        options.addArguments("--profile-directory=" + Configuration.chromeProfileDirectory());
        // Open a single clean tab instead of restoring the profile's saved tabs,
        // and suppress the "restore pages?" / "Chrome didn't shut down properly"
        // bubbles that otherwise appear when launching against a copied profile.
        options.addArguments(
                "--no-first-run",
                "--no-default-browser-check",
                "--restore-last-session=false",
                "--disable-session-crashed-bubble",
                "--hide-crash-restore-bubble",
                "--disable-infobars",
                "--disable-popup-blocking");
        LOG.info("Using persistent Chrome profile at '{}' (profile '{}')",
                userDataDir, Configuration.chromeProfileDirectory());
    }

    /**
     * Resolves the Chrome user-data directory: the configured value if set,
     * otherwise a dedicated, non-default automation profile under the home
     * directory (created by Chrome on first use).
     */
    private static String resolveChromeUserDataDir() {
        String configured = Configuration.chromeUserDataDir();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return System.getProperty("user.home")
                + File.separator + ".wallethub-selenium"
                + File.separator + "chrome-profile";
    }

    /** @return {@code true} if {@code dir} is Chrome's own default profile location. */
    private static boolean isDefaultChromeDir(String dir) {
        return normalize(dir).equals(normalize(osDefaultChromeDir()));
    }

    private static String normalize(String path) {
        return path.replace('\\', '/').replaceAll("/+$", "");
    }

    private static String osDefaultChromeDir() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return home + "/Library/Application Support/Google/Chrome";
        }
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            String base = (localAppData != null && !localAppData.isBlank())
                    ? localAppData
                    : home + "\\AppData\\Local";
            return base + "\\Google\\Chrome\\User Data";
        }
        return home + "/.config/google-chrome";
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return options;
    }
}
