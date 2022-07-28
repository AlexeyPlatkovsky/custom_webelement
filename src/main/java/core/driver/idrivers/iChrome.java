package core.driver.idrivers;

import core.driver.BrowserNames;
import core.driver.DriverCapabilities;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utils.properties.SystemProperties;

public class iChrome extends iDriver {

  public iChrome() {
    WebDriverManager.chromedriver().driverVersion(SystemProperties.BROWSER_VERSION).setup();
    driver = new ChromeDriver((ChromeOptions) (new DriverCapabilities(BrowserNames.CHROME)).getCapabilities());
  }
}
