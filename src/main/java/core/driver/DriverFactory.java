package core.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<DriverNames> DRIVER_NAME = new ThreadLocal<>();

    static {
        configureWebDriverManagerCache();
    }


    public static WebDriver getCurrentDriver() {
        if (DRIVER.get() != null)
            return DRIVER.get();
        else return initDriver();
    }

    public static WebDriver getCurrentDriverOrNull() {
        return DRIVER.get();
    }

    public static WebDriver initDriver() {
        DriverNames driverName = DriverNames.valueOf(SystemProperties.DRIVER.toUpperCase());
        DRIVER_NAME.set(driverName);
        iLogger.info("Create driver " + driverName);

        switch (driverName) {
            case CHROME -> createChromeDriver();
            case FIREFOX -> createFirefoxDriver();
            case LAMBDA -> createLambdaDriver();
            default -> throw new IllegalArgumentException("Invalid driver name: " + driverName);
        }

        if (SystemProperties.SCREEN_MAXIMIZE)
            DRIVER.get().manage().window().maximize();

        return DRIVER.get();
    }

    private static void createChromeDriver() {
        WebDriverManager.chromedriver().driverVersion(SystemProperties.BROWSER_VERSION).setup();
        DRIVER.set(new ChromeDriver((ChromeOptions) DriverCaps.getCaps(DriverNames.CHROME)));
    }

    private static void createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        DRIVER.set(new FirefoxDriver((FirefoxOptions) (DriverCaps.getCaps(DriverNames.FIREFOX))));
    }

    private static void createLambdaDriver() {
        MutableCapabilities capabilities = DriverCaps.getCaps(DriverNames.LAMBDA);
        String accessUrl = RemoteEnvProperties.REMOTE_URL_KEY;

        try {
            DRIVER.set(new RemoteWebDriver(new URL(accessUrl), capabilities));
            iLogger.info("Remote Lambda Driver is created. Remote session is starting.");
        } catch (Exception e) {
            throw new IllegalStateException("Remote WebDriver creation failed: " + e.getMessage(), e);
        }
    }

    public static DriverNames driverName() {
        return DRIVER_NAME.get();
    }

    public static void disposeCurrentDriver() {
        WebDriver driver = DRIVER.get();
        if (Objects.isNull(driver)) {
            return;
        }

        try {
            driver.quit();
        } finally {
            DRIVER.remove();
            DRIVER_NAME.remove();
        }
    }

    private static void configureWebDriverManagerCache() {
        String cacheDirectory = System.getProperty("java.io.tmpdir") + File.separator + "wdm-cache";
        File cacheDir = new File(cacheDirectory);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        System.setProperty("wdm.cachePath", cacheDirectory);
        System.setProperty("wdm.targetPath", cacheDirectory);
    }
}
