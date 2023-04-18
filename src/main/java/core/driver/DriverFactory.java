package core.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

import java.net.URL;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static DriverNames driverName;


    public static WebDriver getCurrentDriver() {
        if (DRIVER.get() != null)
            return DRIVER.get();
        else return initDriver();
    }

    public static WebDriver initDriver() {
        driverName = DriverNames.valueOf(SystemProperties.DRIVER.toUpperCase());
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
            Assert.fail("Remote Web Driver creation failed. " + e);
        }
    }

    public static DriverNames driverName() {
        return driverName;
    }
}
