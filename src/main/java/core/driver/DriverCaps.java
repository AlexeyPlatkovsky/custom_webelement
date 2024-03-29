package core.driver;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

public class DriverCaps {
    private static MutableCapabilities capabilities;

    public static MutableCapabilities getCaps(DriverNames driverName) {
        switch (driverName) {
            case CHROME -> capabilities = getChromeCaps();
            case FIREFOX -> capabilities = getFirefoxCas();
            case LAMBDA -> capabilities = getLambdaCaps();
        }

        iLogger.info("Driver options are : {}", capabilities.toString());
        return capabilities;
    }

    public static MutableCapabilities getChromeCaps() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=" + SystemProperties.SCREEN_RESOLUTION)
                .addArguments("--lang=ru-RU")
                .addArguments("--ignore-certificate-errors")
                .addArguments("--disable-extensions")
                .addArguments("--no-sandbox")
                .addArguments("--remote-allow-origins=*");

        if (SystemProperties.OS.equalsIgnoreCase("unix")) {
            options.addArguments("--headless")
                    .addArguments("--disable-gpu");
        }
        return options;
    }

    public static MutableCapabilities getFirefoxCas() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--window-size=" + SystemProperties.SCREEN_RESOLUTION)
                .addArguments("--lang=ru-RU")
                .addArguments("--remote-allow-origins=*");
        return options;
    }

    public static MutableCapabilities getLambdaCaps() {
        MutableCapabilities remoteCapabilities = new MutableCapabilities();
        remoteCapabilities.setCapability("browser", SystemProperties.REMOTE_BROWSER);
        remoteCapabilities.setCapability("version", SystemProperties.BROWSER_VERSION);
        remoteCapabilities.setCapability("platform", SystemProperties.PLATFORM);
        remoteCapabilities.setCapability("resolution", SystemProperties.SCREEN_RESOLUTION);
        remoteCapabilities.setCapability("user", SystemProperties.REMOTE_USERNAME);
        remoteCapabilities.setCapability("accessKey", SystemProperties.REMOTE_KEY);
        remoteCapabilities.setCapability("build", SystemProperties.BUILD_NUMBER);
        remoteCapabilities.setCapability("timezone", "UTC+00:00");
        remoteCapabilities.setCapability("--lang=", SystemProperties.LOCALE);

        if (!RemoteEnvProperties.USE_LOCAL_PORT.isEmpty()) {
            remoteCapabilities.setCapability("tunnel", true);
        }

        return remoteCapabilities;
    }
}