package com.wallethub.qa.driver;

import com.wallethub.qa.config.Configuration;
import java.net.InetSocketAddress;
import java.net.Socket;
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
            case "chrome" -> createChromeDriver(headless);
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            case "edge" -> new EdgeDriver(edgeOptions(headless));
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '" + browser + "'. Use chrome, firefox or edge.");
        };

        driver.manage().timeouts().pageLoadTimeout(Configuration.pageLoadTimeout());
        driver.manage().window().maximize();
        return driver;
    }

    /**
     * Creates a Chrome driver. If {@code chrome.debugger.address} is set, first
     * tries to attach to that already-running Chrome (reusing your signed-in
     * session); if nothing is listening there it falls back to launching a fresh
     * browser, so a plain run never breaks.
     */
    private static WebDriver createChromeDriver(boolean headless) {
        String debuggerAddress = Configuration.chromeDebuggerAddress();
        if (debuggerAddress != null && !debuggerAddress.isBlank()) {
            String address = debuggerAddress.trim();
            if (isReachable(address)) {
                try {
                    ChromeOptions attach = new ChromeOptions();
                    attach.setExperimentalOption("debuggerAddress", address);
                    WebDriver driver = new ChromeDriver(attach);
                    LOG.info("Attached to running Chrome at {} (using your existing session)", address);
                    return driver;
                } catch (RuntimeException e) {
                    LOG.warn("Attach to Chrome at {} failed ({}) - launching a fresh browser.",
                            address, e.getMessage());
                }
            } else {
                LOG.info("No Chrome is listening on {} - launching a fresh browser. To reuse your "
                        + "signed-in session, run scripts/launch-chrome-debug.sh, sign in, then re-run.",
                        address);
            }
        }
        return new ChromeDriver(freshChromeOptions(headless));
    }

    /** Quick TCP probe so a plain run doesn't hang ~60s when no debug Chrome is up. */
    private static boolean isReachable(String hostPort) {
        int colon = hostPort.lastIndexOf(':');
        if (colon < 0) {
            return false;
        }
        try (Socket socket = new Socket()) {
            String host = hostPort.substring(0, colon);
            int port = Integer.parseInt(hostPort.substring(colon + 1));
            socket.connect(new InetSocketAddress(host, port), 1500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static ChromeOptions freshChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
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
