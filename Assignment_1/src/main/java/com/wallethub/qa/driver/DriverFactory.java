package com.wallethub.qa.driver;

import com.wallethub.qa.config.Configuration;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
            // drives that already-open browser - your real, logged-in session and
            // tabs - instead of launching a fresh, automation-flagged one. Because
            // the browser is already running, no other launch options apply.
            options.setExperimentalOption("debuggerAddress", debuggerAddress.trim());
            LOG.info("Attaching to running Chrome at {} (using your existing session)",
                    debuggerAddress.trim());
            return options;
        }

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        // Soften the most obvious "automated browser" signals for the fresh launch.
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));

        String userDataDir = Configuration.chromeUserDataDir();
        if (userDataDir != null && !userDataDir.isBlank()) {
            // Launch against a specific profile directory - e.g. a COPY of your
            // real profile - so the browser is already signed in and runs as a
            // real user. Point this at a copy, NOT your live default profile:
            // Chrome 136+ refuses to automate the default profile.
            if (isDefaultChromeDir(userDataDir)) {
                throw new IllegalStateException(
                        "chrome.user.data.dir points at Chrome's default profile ('"
                                + userDataDir + "'), which Chrome 136+ will not automate. "
                                + "Point it at a COPY of your profile instead.");
            }
            options.addArguments("--user-data-dir=" + userDataDir.trim());
            options.addArguments("--profile-directory=" + Configuration.chromeProfileDirectory());
            LOG.info("Using Chrome profile at '{}' (profile '{}')",
                    userDataDir.trim(), Configuration.chromeProfileDirectory());
        }

        if (headless) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return options;
    }

    /** @return {@code true} if {@code dir} is Chrome's own default profile location. */
    private static boolean isDefaultChromeDir(String dir) {
        return normalizePath(dir).equals(normalizePath(osDefaultChromeDir()));
    }

    private static String normalizePath(String path) {
        return path.trim().replace('\\', '/').replaceAll("/+$", "");
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
