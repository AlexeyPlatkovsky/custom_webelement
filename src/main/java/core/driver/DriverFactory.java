package core.driver;

import core.driver.idrivers.*;
import core.driver.idrivers.factories.BrowserFactory;
import core.driver.idrivers.factories.iChromeFactory;
import core.driver.idrivers.factories.iFireFoxFactory;
import core.driver.idrivers.factories.iRemoteFactory;
import org.openqa.selenium.WebDriver;
import utils.logging.iLogger;
import utils.properties.SystemProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {

  private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
  private static DriverNames driverName;
  private static final Map<DriverNames, BrowserFactory> IDRIVERS;

  static {
    final Map<DriverNames, BrowserFactory> factories = new HashMap<>();
    factories.put(DriverNames.CHROME, new iChromeFactory());
    factories.put(DriverNames.FIREFOX, new iFireFoxFactory());
    factories.put(DriverNames.REMOTE, new iRemoteFactory());

    IDRIVERS = Collections.unmodifiableMap(factories);
  }

  public static WebDriver initDriver() {
    driverName = DriverNames.valueOf(SystemProperties.DRIVER.toUpperCase());
    iLogger.info("Create driver " + driverName);
    DRIVER.set(IDRIVERS.get(driverName).initBrowser().getDriver());
    return DRIVER.get();
  }

  public static WebDriver getCurrentDriver() {
    return DRIVER.get();
  }

  public static DriverNames driverName() {
    return driverName;
  }
}
