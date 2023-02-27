package core.driver.idrivers;

import core.driver.idrivers.capabilities.DriverCapabilities;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utils.properties.SystemProperties;

public class iChrome extends iDriver {

  public iChrome() {
  }

  @Override
  public void initDriver() {
    WebDriverManager.chromedriver().driverVersion(SystemProperties.REMOTE_BROWSER_VERSION).setup();
    driver = new ChromeDriver((ChromeOptions) (new DriverCapabilities(DriverNames.CHROME)).getCapabilities());
  }
}
