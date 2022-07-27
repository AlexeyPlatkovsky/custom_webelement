package core;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariOptions;
import utils.logging.iLogger;
import utils.properties.EnvProperties;
import utils.properties.SystemProperties;

public class DriverCapabilities {

  private MutableCapabilities capabilities;

  public DriverCapabilities(BrowserNames browser) {
    switch (browser) {
      case CHROME:
        capabilities = new ChromeOptions();;
        break;
      case FIREFOX:
        capabilities = new FirefoxOptions();
        break;
      case EDGE:
        capabilities = new EdgeOptions();
        break;
      case SAFARI:
        capabilities = new SafariOptions();
        break;
      case IE11:
        capabilities = new InternetExplorerOptions();
        break;
      default:
        break;
    }
  }

  public MutableCapabilities getCapabilities() {
    iLogger.info("Browser options are : {}", capabilities.toString());
    return capabilities;
  }

  public void setRemoteOptions() {
    capabilities.setCapability("version", SystemProperties.BROWSER_VERSION);
    capabilities.setCapability("platform", SystemProperties.PLATFORM);
  }

  public void setRemoteTestOptions(String testName, String buildNumber) {
    capabilities.setCapability("resolution", SystemProperties.SCREEN_RESOLUTION);
    capabilities.setCapability("user", SystemProperties.REMOTE_USERNAME);
    capabilities.setCapability("accessKey", SystemProperties.REMOTE_KEY);
    capabilities.setCapability("name", testName);
    capabilities.setCapability("build", buildNumber);
    capabilities.setCapability("timezone", "UTC+00:00");

    if (!EnvProperties.USE_LOCAL_PORT.isEmpty()) {
      capabilities.setCapability("tunnel", true);
    }
  }
}
