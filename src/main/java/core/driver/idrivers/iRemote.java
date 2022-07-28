package core.driver.idrivers;

import core.driver.BrowserNames;
import core.driver.DriverCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import utils.logging.iLogger;
import utils.properties.EnvProperties;
import utils.properties.SystemProperties;

import java.net.URL;

public class iRemote extends iDriver {

  public iRemote() {
    DriverCapabilities options = new DriverCapabilities(BrowserNames.valueOf(SystemProperties.BROWSER.toUpperCase()));
    options.setRemoteOptions();
    String accessUrl = EnvProperties.REMOTE_URL_KEY;
    options.setRemoteTestOptions(SystemProperties.BUILD_NUMBER);
    try {
      driver = new RemoteWebDriver(new URL(accessUrl), options.getCapabilities());
      iLogger.info("Driver created. Remote session starting.");
    } catch (Exception e) {
      Assert.fail("Remote Web Driver creation failed. " + e);
    }
  }
}
