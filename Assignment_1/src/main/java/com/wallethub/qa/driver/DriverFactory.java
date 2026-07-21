package com.wallethub.qa.driver;

import com.wallethub.qa.config.Configuration;
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
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        applyExistingProfile(options);
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return options;
    }

    /**
     * Points Chrome at the user's existing profile (when enabled) so the browser
     * keeps its saved cookies and signed-in session. A returning, logged-in
     * session is far less likely to face a CAPTCHA than a fresh, empty one.
     *
     * <p>Chrome refuses to share a profile that another running instance already
     * holds, so Chrome must be fully quit before the test starts.
     */
    private static void applyExistingProfile(ChromeOptions options) {
        if (!Configuration.useExistingChromeProfile()) {
            return;
        }
        String userDataDir = resolveChromeUserDataDir();
        options.addArguments("--user-data-dir=" + userDataDir);
        options.addArguments("--profile-directory=" + Configuration.chromeProfileDirectory());
        LOG.info("Reusing existing Chrome profile at '{}' (profile '{}')",
                userDataDir, Configuration.chromeProfileDirectory());
    }

    /**
     * Resolves the Chrome user-data directory: the configured value if set,
     * otherwise the OS-default location for the current platform.
     */
    private static String resolveChromeUserDataDir() {
        String configured = Configuration.chromeUserDataDir();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
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
