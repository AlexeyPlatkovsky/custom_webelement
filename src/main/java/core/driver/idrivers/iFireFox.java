package core.driver.idrivers;

import core.driver.BrowserNames;
import core.driver.DriverCapabilities;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class iFireFox extends iDriver {

  public iFireFox() {
    super();
  }

  @Override
  public void initDriver() {
    WebDriverManager.firefoxdriver().setup();
    driver = new FirefoxDriver((FirefoxOptions) (new DriverCapabilities(BrowserNames.FIREFOX)).getCapabilities());
  }
}
