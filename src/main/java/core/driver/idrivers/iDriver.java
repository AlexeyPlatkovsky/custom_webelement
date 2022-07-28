package core.driver.idrivers;

import org.openqa.selenium.WebDriver;

public abstract class iDriver {
  protected WebDriver driver;

  public WebDriver getDriver() {
    return driver;
  }
}
