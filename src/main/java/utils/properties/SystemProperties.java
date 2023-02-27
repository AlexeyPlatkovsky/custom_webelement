package utils.properties;

import utils.properties.annotations.Property;

public class SystemProperties {
    @Property(value = "base_url")
    public static String BASE_URL;

    @Property(value = "build_number")
    public static String BUILD_NUMBER;

    @Property(value = "driver")
    public static String DRIVER;

    @Property(value = "locale")
    public static String LOCALE;

    @Property(value = "platform")
    public static String PLATFORM;

    @Property(value = "remote_browser")
    public static String REMOTE_BROWSER;

    @Property(value = "remote_browser_version")
    public static String REMOTE_BROWSER_VERSION;

    @Property(value = "remote_key")
    public static String REMOTE_KEY;

    @Property(value = "remote_username")
    public static String REMOTE_USERNAME;

    @Property(value = "screen_maximize")
    public static Boolean SCREEN_MAXIMIZE;

    @Property(value = "screen_resolution")
    public static String SCREEN_RESOLUTION;

    @Property(value = "test_rail_url")
    public static String TEST_RAIL_URL;
}
