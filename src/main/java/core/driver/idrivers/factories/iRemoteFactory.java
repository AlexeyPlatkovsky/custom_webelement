package core.driver.idrivers.factories;

import core.driver.idrivers.iDriver;
import core.driver.idrivers.iRemote;

public class iRemoteFactory implements BrowserFactory{
  @Override
  public iDriver initBrowser() {
    return new iRemote();
  }
}
