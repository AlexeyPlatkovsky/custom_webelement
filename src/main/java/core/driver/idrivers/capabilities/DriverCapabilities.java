package core.driver.idrivers.capabilities;

import core.driver.idrivers.DriverNames;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

import java.util.Map;

public class DriverCapabilities {
  private final MutableCapabilities capabilities;
  private static final Map<DriverNames, MutableCapabilities> CAPS;

  static {
    CAPS = Map.of(DriverNames.CHROME, new iChromeCapabilities().options,
            DriverNames.FIREFOX, new FirefoxOptions(),
            DriverNames.REMOTE, new iChromeCapabilities().options
    );
  }

  public DriverCapabilities(DriverNames browser) {
    capabilities = CAPS.get(browser);
  }

  public MutableCapabilities getCapabilities() {
    iLogger.info("Browser options are : {}", capabilities.toString());
    return capabilities;
  }

  public void setRemoteOptions(String buildNumber) {
    capabilities.setCapability("browser", SystemProperties.REMOTE_BROWSER);
    capabilities.setCapability("version", SystemProperties.REMOTE_BROWSER_VERSION);
    capabilities.setCapability("platform", SystemProperties.PLATFORM);
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
