package core.driver.idrivers.factories;

import core.driver.idrivers.iDriver;
import core.driver.idrivers.iFireFox;

public class iFireFoxFactory implements BrowserFactory{
  @Override
  public iDriver initBrowser() {
    return new iFireFox();
  }
}
