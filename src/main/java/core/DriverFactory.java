package core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import utils.logging.iLogger;
import utils.properties.EnvProperties;
import utils.properties.SystemProperties;

import java.net.URL;

public class DriverFactory {

  private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
  private static DriverNames driverName;

  public static WebDriver getDriver(String testName) throws Exception {
    driverName = DriverNames.valueOf(SystemProperties.DRIVER.toUpperCase());
    iLogger.info("Create driver " + driverName);
    switch (driverName) {
      case CHROME:
        WebDriverManager.chromedriver().driverVersion(SystemProperties.BROWSER_VERSION).setup();
        DRIVER.set(new ChromeDriver((ChromeOptions) (new DriverCapabilities(BrowserNames.CHROME)).getCapabilities()));
        return DRIVER.get();
      case FIREFOX:
        WebDriverManager.firefoxdriver().setup();
        DRIVER
                .set(new FirefoxDriver((FirefoxOptions) (new DriverCapabilities(BrowserNames.FIREFOX)).getCapabilities()));
        return DRIVER.get();
      case REMOTE:
        DriverCapabilities options = new DriverCapabilities(
                BrowserNames.valueOf(SystemProperties.BROWSER.toUpperCase()));
        options.setRemoteOptions();
        String accessUrl = EnvProperties.LAMBDA_URL_KEY;
        options.setLambdaTestOptions(testName, SystemProperties.BUILD_NUMBER);
        try {
          DRIVER.set(new RemoteWebDriver(new URL(accessUrl), options.getCapabilities()));
          iLogger.info("Driver created. Remote session starting.");
        } catch (Exception e) {
          Assert.fail("Remote Web Driver creation failed. " + e.toString());
        }
        return DRIVER.get();
      default:
        throw new Exception("No such driver in DriverFactory");
    }
  }

  public static WebDriver getCurrentDriver() {
    return DRIVER.get();
  }

  public static DriverNames driverName() {
    return driverName;
  }
}
