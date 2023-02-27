package core.driver.idrivers;

import core.driver.idrivers.capabilities.DriverCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

import java.net.URL;

public class iLambda extends iDriver {

  public iLambda() {
  }

  @Override
  public void initDriver() {
    DriverCapabilities options = new DriverCapabilities(DriverNames.valueOf(SystemProperties.REMOTE_BROWSER.toUpperCase()));
    options.setRemoteOptions(SystemProperties.BUILD_NUMBER);
    String accessUrl = RemoteEnvProperties.REMOTE_URL_KEY;

    try {
      driver = new RemoteWebDriver(new URL(accessUrl), options.getCapabilities());
      iLogger.info("Driver created. Remote session starting.");
    } catch (Exception e) {
      Assert.fail("Remote Web Driver creation failed. " + e);
    }
  }
}
