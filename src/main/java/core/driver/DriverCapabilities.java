package core.driver;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariOptions;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

import java.util.Map;

public class DriverCapabilities {

  private final MutableCapabilities capabilities;
  private static final Map<BrowserNames, MutableCapabilities> CAPS;

  static {

    CAPS = Map.of(BrowserNames.CHROME, new ChromeOptions().addArguments("--lang=" + SystemProperties.LOCALE),
            BrowserNames.FIREFOX, new FirefoxOptions(),
            BrowserNames.EDGE, new EdgeOptions(),
            BrowserNames.SAFARI, new SafariOptions(),
            BrowserNames.IE11, new InternetExplorerOptions());
  }

  public DriverCapabilities(BrowserNames browser) {
    capabilities = CAPS.get(browser);
  }

  public MutableCapabilities getCapabilities() {
    iLogger.debug("Browser options are : {}", capabilities.toString());
    return capabilities;
  }

  public void setRemoteOptions() {
    capabilities.setCapability("version", SystemProperties.BROWSER_VERSION);
    capabilities.setCapability("platform", SystemProperties.PLATFORM);
  }

  public void setRemoteTestOptions(String buildNumber) {
    capabilities.setCapability("resolution", SystemProperties.SCREEN_RESOLUTION);
    capabilities.setCapability("user", SystemProperties.REMOTE_USERNAME);
    capabilities.setCapability("accessKey", SystemProperties.REMOTE_KEY);
    capabilities.setCapability("build", buildNumber);
    capabilities.setCapability("timezone", "UTC+00:00");
    capabilities.setCapability("--lang=", SystemProperties.LOCALE);

    if (!RemoteEnvProperties.USE_LOCAL_PORT.isEmpty()) {
      capabilities.setCapability("tunnel", true);
    }
  }
}
