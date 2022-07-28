package core.driver;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariOptions;
import utils.logging.iLogger;
import utils.properties.EnvProperties;
import utils.properties.SystemProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DriverCapabilities {

  private final MutableCapabilities capabilities;
  private static final Map<BrowserNames, MutableCapabilities> CAPS;

  static {
    final Map<BrowserNames, MutableCapabilities> browserCapabilities = new HashMap<>();
    browserCapabilities.put(BrowserNames.CHROME, new ChromeOptions());
    browserCapabilities.put(BrowserNames.FIREFOX, new FirefoxOptions());
    browserCapabilities.put(BrowserNames.EDGE, new EdgeOptions());
    browserCapabilities.put(BrowserNames.SAFARI, new SafariOptions());
    browserCapabilities.put(BrowserNames.IE11, new InternetExplorerOptions());

    CAPS = Collections.unmodifiableMap(browserCapabilities);
  }

  public DriverCapabilities(BrowserNames browser) {
    capabilities = CAPS.get(browser);
  }

  public MutableCapabilities getCapabilities() {
    iLogger.info("Browser options are : {}", capabilities.toString());
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

    if (!EnvProperties.USE_LOCAL_PORT.isEmpty()) {
      capabilities.setCapability("tunnel", true);
    }
  }
}
