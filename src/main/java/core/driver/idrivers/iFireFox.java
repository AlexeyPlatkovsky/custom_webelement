package core.driver.idrivers;

import core.driver.idrivers.capabilities.DriverCapabilities;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class iFireFox extends iDriver {

  public iFireFox() {
    super();
  }

  @Override
  public void initDriver() {
    System.setProperty("webdriver.gecko.driver","./src/main/resources/geckodriver.exe" );
    driver = new FirefoxDriver((FirefoxOptions) (new DriverCapabilities(DriverNames.FIREFOX)).getCapabilities());
  }
}
