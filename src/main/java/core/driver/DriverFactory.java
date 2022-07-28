package core.driver;

import core.driver.idrivers.*;
import org.openqa.selenium.WebDriver;
import utils.logging.iLogger;
import utils.properties.SystemProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {

  private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
  private static DriverNames driverName;
  private static final Map<DriverNames, iDriver> IDRIVERS;

  static {
    final Map<DriverNames, iDriver> drivers = new HashMap<>();
    drivers.put(DriverNames.CHROME, new iChrome());
    drivers.put(DriverNames.FIREFOX, new iFireFox());
    drivers.put(DriverNames.REMOTE, new iRemote());

    IDRIVERS = Collections.unmodifiableMap(drivers);
  }

  public static WebDriver getDriver() {
    driverName = DriverNames.valueOf(SystemProperties.DRIVER.toUpperCase());
    iLogger.info("Create driver " + driverName);
    DRIVER.set(IDRIVERS.get(driverName).getDriver());
    return DRIVER.get();
  }

  public static WebDriver getCurrentDriver() {
    return DRIVER.get();
  }

  public static DriverNames driverName() {
    return driverName;
  }
}
