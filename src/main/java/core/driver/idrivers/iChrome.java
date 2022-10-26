package core.driver.idrivers;

import core.driver.BrowserNames;
import core.driver.idrivers.capabilities.DriverCapabilities;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import utils.properties.SystemProperties;

import java.sql.SQLOutput;

public class iChrome extends iDriver {

  public iChrome() {
  }

  @Override
  public void initDriver() {
    WebDriverManager.chromedriver().driverVersion(SystemProperties.BROWSER_VERSION).setup();
    driver = new ChromeDriver((ChromeOptions) (new DriverCapabilities(BrowserNames.CHROME)).getCapabilities());
  }
}
