package utils.properties;

public class SystemProperties {
  @Property(value = "base_url")
  public static String BASE_URL;

  @Property(value = "browser")
  public static String BROWSER;

  @Property(value = "browser_version")
  public static String BROWSER_VERSION;

  @Property(value = "build_number")
  public static String BUILD_NUMBER;

  @Property(value = "driver")
  public static String DRIVER;

  @Property(value = "locale")
  public static String LOCALE;

  @Property(value = "platform")
  public static String PLATFORM;

  @Property(value = "remote_key")
  public static String REMOTE_KEY;

  @Property(value = "remote_username")
  public static String REMOTE_USERNAME;

  @Property(value = "screen_resolution")
  public static String SCREEN_RESOLUTION;

  @Property(value = "test_rail_url")
  public static String TEST_RAIL_URL;
}
