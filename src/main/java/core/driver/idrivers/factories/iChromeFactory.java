package core.driver.idrivers.factories;

import core.driver.idrivers.iChrome;
import core.driver.idrivers.iDriver;

public class iChromeFactory implements BrowserFactory {

  @Override
  public iDriver initBrowser() {
    return new iChrome();
  }
}
